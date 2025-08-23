package lovexyn0827.mess.electronic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL21;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.electronic.Oscilscope.Channel;
import lovexyn0827.mess.electronic.Oscilscope.Edge;
import lovexyn0827.mess.util.FormattedText;
import lovexyn0827.mess.util.ServerMicroTime;
import lovexyn0827.mess.util.i18n.I18N;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public final class OscilscopeScreen extends Screen {
	private static final int VALID_TICKING_STAGE_COUNT = (int) (Arrays.stream(ServerTickingPhase.values())
			.filter((phase) -> !phase.notInAnyWorld)
			.count());
	private static final Object2IntMap<ServerTickingPhase> VALID_TICKING_STAGE_INDICES = Util.make(() -> {
		int idx = 0;
		Object2IntOpenHashMap<ServerTickingPhase> result = new Object2IntOpenHashMap<>();
		for (ServerTickingPhase phase : ServerTickingPhase.values()) {
			if (phase.notInAnyWorld) {
				continue;
			}
			
			result.put(phase, idx++);
		}
		
		return result;
	});
	private WaveArea waveArea;
	private TrigHistory trigHistory;
	private ChannelList channelList;
	private final OscilscopeDataStorage waveData;
	private ButtonWidget modeBtn;
	private boolean digitalMode = false;
	private CheckboxWidget tickAlignedBtn;
	private TextFieldWidget stroageDepth;
	private ButtonWidget resetBtn;
	private SliderWidget waveTransparancySlider;
	private int waveTransparancy;
	
	public OscilscopeScreen() {
		super(I18N.translateAsText("oscilscope.gui.title"));
		this.waveData = OscilscopeDataStorage.get();
	}
	
	@Override
	public void init() {
		this.width = this.client.getWindow().getScaledWidth();
		this.height = this.client.getWindow().getScaledHeight();
		this.updateWaveArea();
		this.trigHistory = new TrigHistory();
		this.channelList = new ChannelList();
		this.addChild(this.trigHistory);
		this.addChild(this.channelList);
		this.waveData.getAllChannels().forEach(this::onNewChannel);
		this.waveData.getTriggerHistory().forEach(this.trigHistory::onTrig);
		this.modeBtn = new ButtonWidget(
				(int) (this.width * 0.5) - 90, (int) (this.height * 0.6) + 16, 
				80, 20, 
				I18N.translateAsText(this.digitalMode ? "oscil.gui.mode.digital" : "oscil.gui.mode.analog"), 
				(btn) -> {
					this.digitalMode ^= true;
					btn.setMessage(I18N.translateAsText(
							this.digitalMode ? "oscil.gui.mode.digital" : "oscil.gui.mode.analog"));
					this.updateWaveArea();
					MessMod.INSTANCE.getOscilscope().setDigitalMode(this.digitalMode);
				});
		this.addButton(this.modeBtn);
		this.tickAlignedBtn = new CheckboxWidget(
				(int) (this.width * 0.5) + 10, (int) (this.height * 0.6) + 16, 
				80, 20, I18N.translateAsText("oscil.gui.tickaligned"), false);
		this.addButton(this.tickAlignedBtn);
		this.stroageDepth = new TextFieldWidget(this.textRenderer, 
				(int) (this.width * 0.5) - 90, (int) (this.height * 0.6) + 38, 
				80, 20, I18N.translateAsText("oscil.gui.storage"));
		this.stroageDepth.setTextPredicate((in) -> in.matches("^[0-9]*$"));
		this.stroageDepth.setChangedListener((in) -> {
			if (!in.isEmpty()) {
				this.waveData.setStorageDepth(Integer.parseInt(in));
			}
		});
		this.stroageDepth.setText(Integer.toString(this.waveData.getStorageDepth()));
		this.addChild(this.stroageDepth);
		this.resetBtn = new ButtonWidget(
				(int) (this.width * 0.5) + 10, (int) (this.height * 0.6) + 38, 
				80, 20, 
				I18N.translateAsText("oscil.gui.reset"), 
				(btn) -> {
					this.waveData.reset();
					this.trigHistory.reset();
					this.waveArea.leftTick = MessMod.INSTANCE.getGameTime();
				});
		this.addButton(this.resetBtn);
		this.waveTransparancySlider = new SliderWidget(
				(int) (this.width * 0.5) - 90, (int) (this.height * 0.6) + 60, 
				180, 20, Text.of(String.format("%d / %d", this.waveTransparancy, 255)), this.waveTransparancy) {
			@Override
			protected void updateMessage() {
				this.setMessage(Text.of(String.format("%d / %d", (int) (this.value * 255), 255)));
			}

			@Override
			protected void applyValue() {
				OscilscopeScreen.this.waveTransparancy = (int) (this.value * 255);
			}
		};
		this.addChild(this.waveTransparancySlider);
	}
	
	private void updateWaveArea() {
		this.children.remove(this.waveArea);
		this.waveArea = this.digitalMode ? new DigitalWaveArea() : new AnalogWaveArea();
		this.addChild(this.waveArea);
		this.waveArea.recalculateWaveAreaParameters();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.waveData.takeNewChannels(this::onNewChannel);
		this.waveData.takeUnprocessedTriggers(this::onTrig);
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.trigHistory.render(matrices, mouseX, mouseY, delta);
		this.channelList.render(matrices, mouseX, mouseY, delta);
		this.modeBtn.render(matrices, mouseX, mouseY, delta);
		this.tickAlignedBtn.render(matrices, mouseX, mouseY, delta);
		this.stroageDepth.render(matrices, mouseX, mouseY, delta);
		this.resetBtn.render(matrices, mouseX, mouseY, delta);
		this.waveTransparancySlider.render(matrices, mouseX, mouseY, delta);
		this.waveArea.render(matrices, mouseX, mouseY, delta);
	}
	
	private void onTrig(Oscilscope.Trigger trig) {
		this.waveArea.scrollToTrig(trig.time.gameTime);
		this.trigHistory.onTrig(trig);
	}
	
	private void onNewChannel(Oscilscope.Channel channel) {
		this.channelList.newChannel(channel);
	}
	
	private Element getHoveringElement(double mouseX, double mouseY) {
		for (Element e : this.children) {
			if (e.isMouseOver(mouseX, mouseY)) {
				return e;
			}
		}
		
		if (mouseY > this.height * 0.6 + 16) {
			return mouseY > this.width / 2 ? this.channelList : this.trigHistory;
		} else {
			return this.waveArea;
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button)
				|| this.getHoveringElement(mouseX, mouseY).mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return super.mouseReleased(mouseX, mouseY, button)
				|| this.getHoveringElement(mouseX, mouseY).mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
				|| this.getHoveringElement(mouseX, mouseY).mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return super.mouseScrolled(mouseX, mouseY, amount)
				|| this.getHoveringElement(mouseX, mouseY).mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	private abstract class WaveArea implements Element, Drawable {
		protected int tickPerDiv = 1;
		protected long leftTick = MessMod.INSTANCE.getGameTime();;
		protected int trigHDiv = 0;
		protected int left;
		protected int right;
		protected int top;
		protected int bottom;
		protected int pixelsPerHDiv;
		protected int hDivs;
		private boolean draggingTrigHDivMarker = false;
		
		void recalculateWaveAreaParameters() {
			this.pixelsPerHDiv = VALID_TICKING_STAGE_COUNT * 3;
			this.hDivs = (OscilscopeScreen.this.width - 48) / this.pixelsPerHDiv;
			int waveAreaWidth = this.pixelsPerHDiv * this.hDivs;
			this.left = (OscilscopeScreen.this.width - waveAreaWidth) / 2;
			this.right = this.left + waveAreaWidth;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			int trigHorizontalPos = this.trigHDiv * this.pixelsPerHDiv + this.left;
			if (MathHelper.absMax(mouseX - trigHorizontalPos, mouseY - (this.top - 5)) < 5) {
				this.draggingTrigHDivMarker = true;
				return true;
			}
			
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			this.draggingTrigHDivMarker = false;
			return true;
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (this.draggingTrigHDivMarker) {
				int nearestDiv = (int) Math.round((mouseX - this.left) / this.pixelsPerHDiv);
				this.trigHDiv = MathHelper.clamp(nearestDiv, 0, this.hDivs);
				return true;
			}
			
			return false;
		}
		
		protected int calculateNextDivLength(int prev, boolean inc, int max) {
			if (prev >= max && inc || prev == 1 && !inc) {
				return prev;
			}
			
			double logFrac = Math.log10(prev) - Math.floor(Math.log10(prev));
			// lg 5 <~ 0.7, lg 2 >~ 0.3
			if (logFrac > 0.4) {
				// 5 => 10 : 5 => 2
				return inc ? prev * 2 : prev * 2 / 5;
			} else if (logFrac < 0.3) {
				// 1 => 2 : 10 => 5
				return inc ? prev * 2 : prev / 2;
			} else {
				// 2 => 5 : 2 => 1
				return inc ? prev * 5 / 2 : prev / 2;
			}
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (Screen.hasControlDown()) {
				int prevTickPreDiv = this.tickPerDiv;
				this.tickPerDiv = this.calculateNextDivLength(this.tickPerDiv, amount < 0, 1000000);
				long tickAtCursor = ((long) mouseX - this.left) * prevTickPreDiv / this.pixelsPerHDiv 
						+ this.leftTick;
				this.leftTick = tickAtCursor - ((long) mouseX - this.left) * this.tickPerDiv / this.pixelsPerHDiv;
			} else {
				this.leftTick += -(Screen.hasShiftDown() ? amount * this.hDivs : amount) * this.tickPerDiv;
			}
			
			return true;
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT:
				if (Screen.hasControlDown()) {
					this.tickPerDiv = this.calculateNextDivLength(this.tickPerDiv, false, 1000000);
				} else {
					this.leftTick += (Screen.hasShiftDown() ? -this.hDivs : -1) * this.tickPerDiv;
				}
				
				return true;
			case GLFW.GLFW_KEY_RIGHT:
				if (Screen.hasControlDown()) {
					this.tickPerDiv = this.calculateNextDivLength(this.tickPerDiv, true, 1000000);
				} else {
					this.leftTick += (Screen.hasShiftDown() ? this.hDivs : 1) * this.tickPerDiv;
				}
				
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.drawBackground(matrices);
			this.drawGrids(matrices);
			this.drawAxises(matrices);
			this.drawTrigMarkers(matrices);
			this.drawWaves(matrices, mouseX, mouseY);
			this.drawStatus(matrices);
			this.drawToolTip(matrices, mouseX, mouseY);
		}

		protected void drawToolTip(MatrixStack matrices, int mouseX, int mouseY) {
		}
		
		protected void appendToolTipIfNeeded(List<Text> toolTip, int mouseX, int mouseY, 
				int prevX, int prevY, int curX, int curY, Channel channel, Edge prevEdge, Edge edge) {
			long window = OscilscopeScreen.this.client.getWindow().getHandle();
			if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) != GLFW.GLFW_PRESS) {
				return;
			}
			
			int maxY = prevY > curY ? prevY : curY;
			int minY = prevY < curY ? prevY : curY;
			if (edge != null && MathHelper.abs(mouseX - curX) <= 1 && mouseY > minY && mouseY < maxY) {
				toolTip.add(Text.of(String.format("%d : %s", edge.time.gameTime, edge.time.phase)));
				toolTip.add(Text.of(String.format("  CH%d: %d => %d", 
						channel.getId(), prevEdge.newLevel, edge.newLevel)));
			} else if (MathHelper.abs(mouseY - prevY) <= 1 && mouseX >= prevX && mouseX <= curX) {
				List<Oscilscope.Edge> wave = OscilscopeScreen.this.waveData.getWaveData().get(channel);
				int lastEdgeIdx = Collections.binarySearch(wave, prevEdge, Comparator.comparing((e) -> e.time));
				if (edge == null) {
					if (lastEdgeIdx < wave.size() - 1) {
						edge = wave.get(lastEdgeIdx + 1);
					}
				}
				
				boolean digitalMode = OscilscopeScreen.this.digitalMode;
				boolean prevInvalid = prevEdge.time.gameTime < 0
						|| digitalMode && edge != null && !(prevEdge.newLevel > 0 ^ edge.newLevel > 0);
				// XXX: fetch when fail?
				if (prevInvalid || edge == null) {
					toolTip.add(Text.of(String.format("CH%d: %d, ? gt", channel.getId(), prevEdge.newLevel)));
				} else {
					toolTip.add(Text.of(String.format("CH%d: %d, %d gt", 
							channel.getId(), prevEdge.newLevel, edge.time.gameTime - prevEdge.time.gameTime)));
				}

				if (prevInvalid) {
					toolTip.add(Text.of("From: ?"));
				} else {
					toolTip.add(Text.of(String.format("  From: %d @ %s", 
							prevEdge.time.gameTime, prevEdge.time.phase)));
				}
				
				if (edge != null) {
					toolTip.add(Text.of(String.format("  To: %d @ %s", edge.time.gameTime, edge.time.phase)));
				} else {
					toolTip.add(Text.of("  To: ?"));
				}
			}
		}

		protected abstract void drawStatus(MatrixStack matrices);

		protected void drawBackground(MatrixStack matrices) {
			fill(matrices, this.left, this.top, this.right, this.bottom, 0xFF000000);
		}

		protected abstract void drawWaves(MatrixStack matrices, int mouseX, int mouseY);
		
		protected int getTransformedColor(Oscilscope.Channel ch) {
			return ch.getColor() & (((255 - OscilscopeScreen.this.waveTransparancy) << 24) | 0x00FFFFFF);
		}
		
		protected int calculatePosOfTime(ServerMicroTime t) {
			long relativeTime = t.gameTime - this.leftTick;
			boolean alignToTick = OscilscopeScreen.this.tickAlignedBtn.isChecked();
			long flatTime = relativeTime * VALID_TICKING_STAGE_COUNT 
					+ (alignToTick ? 0 : VALID_TICKING_STAGE_INDICES.getInt(t.phase));
			int raw = (int) (flatTime * this.pixelsPerHDiv / VALID_TICKING_STAGE_COUNT / this.tickPerDiv + this.left);
			return MathHelper.clamp(raw, this.left, this.right);
		}

		private void drawTrigMarkers(MatrixStack matrices) {
			Matrix4f matrix = matrices.peek().getModel();
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableCull();
			bufferBuilder.begin(GL21.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
			this.drawTrigMarkers(matrix, bufferBuilder);
			bufferBuilder.end();
			BufferRenderer.draw(bufferBuilder);
			RenderSystem.enableCull();
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
		}

		protected void drawTrigMarkers(Matrix4f matrix, BufferBuilder bufferBuilder) {
			int trigHorizontalPos = this.trigHDiv * this.pixelsPerHDiv + this.left;
			bufferBuilder.vertex(matrix, trigHorizontalPos, this.top, 0).color(1.0F, 1.0F, 0, 1.0F).next();
			bufferBuilder.vertex(matrix, trigHorizontalPos - 5, this.top - 5, 0).color(1.0F, 1.0F, 0, 1.0F).next();
			bufferBuilder.vertex(matrix, trigHorizontalPos + 5, this.top - 5, 0).color(1.0F, 1.0F, 0, 1.0F).next();
		}

		protected void drawAxises(MatrixStack matrices) {
			int hStep = this.pixelsPerHDiv / VALID_TICKING_STAGE_COUNT;
			for (int i = 0; i <= this.hDivs * VALID_TICKING_STAGE_COUNT; i++) {
				drawVerticalLine(matrices, this.left + i * hStep, this.bottom, this.bottom - 2, 0xFFFFFFFF);
			}
			
			for (int i = 0; i <= this.hDivs; i++) {
				if (i % 5 != 0) {
					continue;
				}
				
				drawCenteredString(matrices, OscilscopeScreen.this.textRenderer, 
						Long.toString(this.getTickAtDiv(i)), 
						this.left + i * this.pixelsPerHDiv, this.bottom + 3, 0xFFFFFFFF);
				drawVerticalLine(matrices, this.left + i * this.pixelsPerHDiv, this.bottom, this.bottom - 3, 
						0xFFFFFFFF);
			}
		}


		private long getTickAtDiv(int i) {
			return this.leftTick + i * this.tickPerDiv;
		}

		protected void drawGrids(MatrixStack matrices) {
			for (int i = 0; i <= this.hDivs; i++) {
				drawVerticalLine(matrices, this.left + i * this.pixelsPerHDiv, this.top, this.bottom, 0x3FFFFFFF);
			}
		}

		public void scrollToTrig(long gameTime) {
			this.leftTick = gameTime - this.tickPerDiv * this.trigHDiv;
		}
	}
	
	private final class AnalogWaveArea extends WaveArea {
		private int levelPerDiv = 1;
		private int bottomLevel = 0;
		private int vDivs;
		private int pixelsPerVDiv;
		private Oscilscope.Channel draggingTrigLevelMarker = null;
		
		@Override
		void recalculateWaveAreaParameters() {
			super.recalculateWaveAreaParameters();
			// Vertical
			this.vDivs = 15;
			this.pixelsPerVDiv = ((int) (OscilscopeScreen.this.height * 0.6 - 8)) / this.vDivs;
			int waveAreaHeight = this.pixelsPerVDiv * this.vDivs;
			this.top = 8;
			this.bottom = this.top + waveAreaHeight;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (super.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}

			Optional<Oscilscope.Channel> dragging = OscilscopeScreen.this.waveData.getAllChannels().stream()
					.filter(Oscilscope.Channel::isVisible)
					.sorted(Comparator.comparing(Oscilscope.Channel::getId))
					.filter((ch) -> {
						int yPos = this.bottom - (ch.getTrigLevel() - this.bottomLevel) * this.pixelsPerHDiv;
						return MathHelper.absMax(mouseX - (this.right + 5), mouseY - this.top - yPos) < 10;
					})
					.findFirst();
			if (dragging.isPresent()) {
				this.draggingTrigLevelMarker = dragging.get();
				return true;
			}
			
			
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			super.mouseReleased(mouseX, mouseY, button);
			this.draggingTrigLevelMarker = null;
			return true;
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
				return true;
			}
			
			if (this.draggingTrigLevelMarker != null) {
				int nearestDiv = (int) Math.round((this.bottom - mouseY) / this.pixelsPerVDiv);
				int clampedDiv = MathHelper.clamp(nearestDiv, 0, this.vDivs);
				this.draggingTrigLevelMarker.setTrigLevel(clampedDiv * this.levelPerDiv + this.bottomLevel);
			}
			
			return true;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (Screen.hasAltDown()) {
				if (Screen.hasControlDown()) {
					this.levelPerDiv = this.calculateNextDivLength(this.levelPerDiv, amount > 0, 100);
				} else {
					this.bottomLevel += (Screen.hasShiftDown() ? amount * this.vDivs : amount) * this.levelPerDiv;
				}
			} else {
				super.mouseScrolled(mouseX, mouseY, amount);
			}
			
			return true;
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			switch (keyCode) {
			case GLFW.GLFW_KEY_UP:
				if (Screen.hasControlDown()) {
					this.levelPerDiv = this.calculateNextDivLength(this.levelPerDiv, true, 500);
				} else {
					this.bottomLevel += (Screen.hasShiftDown() ? this.vDivs : 1) * this.levelPerDiv;
				}
				
				return true;
			case GLFW.GLFW_KEY_DOWN:
				if (Screen.hasControlDown()) {
					this.levelPerDiv = this.calculateNextDivLength(this.levelPerDiv, false, 500);
				} else {
					this.bottomLevel += (Screen.hasShiftDown() ? -this.vDivs : -1) * this.levelPerDiv;
				}
				
				return true;
			default:
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		
		@Override
		protected void drawToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.draggingTrigLevelMarker != null) {
				List<Text> tooltip = Lists.newArrayList(
						Text.of(String.format("CH%d: %d", this.draggingTrigLevelMarker.getId(), 
								this.draggingTrigLevelMarker.getTrigLevel())));
				OscilscopeScreen.this.renderTooltip(matrices, tooltip, mouseX, mouseY);
			}
		}

		@Override
		protected void drawStatus(MatrixStack matrices) {
			drawCenteredString(matrices, OscilscopeScreen.this.textRenderer, 
					String.format("Hori: %d gt / div   Vert: %d level / div", this.tickPerDiv, this.levelPerDiv), 
					OscilscopeScreen.this.width / 2, OscilscopeScreen.this.height - 12, 0xFFFFFFFF);
		}

		@Override
		protected void drawWaves(MatrixStack matrices, int mouseX, int mouseY) {
			long from = this.leftTick;
			long to = this.leftTick + this.hDivs * this.tickPerDiv;
			List<Text> toolTip = new ArrayList<>();
			Map<Oscilscope.Channel, List<Oscilscope.Edge>> data = 
					OscilscopeScreen.this.waveData.getWaveData(from, to, false);
			data.forEach((channel, wave) -> {
				int color = this.getTransformedColor(channel);
				if (wave.isEmpty()) {
					return;
				}
				
				Oscilscope.Edge prevEdge = null;
				for (Oscilscope.Edge edge : wave) {
					if (prevEdge != null) {
						int prevX = this.calculatePosOfTime(prevEdge.time);
						int curX = this.calculatePosOfTime(edge.time);
						int prevY = this.calculatePosOfLevel(prevEdge.newLevel);
						int curY = this.calculatePosOfLevel(edge.newLevel);
						drawHorizontalLine(matrices, prevX, curX, prevY, color);
						drawVerticalLine(matrices, curX, prevY, curY, color);
						this.appendToolTipIfNeeded(toolTip, mouseX, mouseY, prevX, prevY, curX, curY, 
								channel, prevEdge, edge);
					}
					
					prevEdge = edge;
				}
				
				drawHorizontalLine(matrices, this.calculatePosOfTime(prevEdge.time), 
						this.calculatePosOfTime(ServerMicroTime.current()), 
						this.calculatePosOfLevel(prevEdge.newLevel), color);
				this.appendToolTipIfNeeded(toolTip, mouseX, mouseY, 
						this.calculatePosOfTime(prevEdge.time), 
						this.calculatePosOfLevel(prevEdge.newLevel), 
						this.right, -1, channel, prevEdge, null);
			});
			if (!toolTip.isEmpty()) {
				OscilscopeScreen.this.renderTooltip(matrices, toolTip, mouseX, mouseY);
			}
		}
		
		private int calculatePosOfLevel(int level) {
			int raw = this.bottom - (level - this.bottomLevel) * this.pixelsPerVDiv / this.levelPerDiv;
			return MathHelper.clamp(raw, this.top, this.bottom);
		}

		@Override
		protected void drawTrigMarkers(Matrix4f matrix, BufferBuilder bufferBuilder) {
			super.drawTrigMarkers(matrix, bufferBuilder);
			OscilscopeScreen.this.waveData.getAllChannels().stream()
					.filter(Oscilscope.Channel::isVisible)
					.mapToInt((ch) -> this.bottom - (ch.getTrigLevel() - this.bottomLevel) * this.pixelsPerVDiv)
					.forEach((yPos) -> {
						bufferBuilder.vertex(matrix, this.right, yPos, 0).color(1.0F, 1.0F, 0, 1.0F).next();
						bufferBuilder.vertex(matrix, this.right + 5, yPos - 5, 0).color(1.0F, 1.0F, 0, 1.0F).next();
						bufferBuilder.vertex(matrix, this.right + 5, yPos + 5, 0).color(1.0F, 1.0F, 0, 1.0F).next();
					});
		}

		protected void drawAxises(MatrixStack matrices) {
			super.drawAxises(matrices);
			for (int i = 0; i <= this.vDivs; i++) {
				drawCenteredString(matrices, OscilscopeScreen.this.textRenderer, 
						Integer.toString(this.getLevelAtDiv(i)), 
						this.left - 12, this.top + i * this.pixelsPerVDiv - 4, 0xFFFFFFFF);
			}
		}
		
		private int getLevelAtDiv(int i) {
			return this.bottomLevel + (this.vDivs - i) * this.levelPerDiv;
		}


		protected void drawGrids(MatrixStack matrices) {
			super.drawGrids(matrices);
			for (int i = 0; i <= this.vDivs; i++) {
				drawHorizontalLine(matrices, this.left, this.right, this.top + i * this.pixelsPerVDiv, 0x3FFFFFFF);
			}
		}
	}
	
	private final class DigitalWaveArea extends WaveArea {
		private int topChannelId = 0;
		private int displayedChannelCount = 0;
		private int waveHeight = 8;
		
		@Override
		void recalculateWaveAreaParameters() {
			super.recalculateWaveAreaParameters();
			int maxHeight = ((int) (OscilscopeScreen.this.height * 0.6 - 8));
			this.displayedChannelCount = (maxHeight - 3 * this.waveHeight) / (this.waveHeight * 2);
			int height = (this.displayedChannelCount * 2 + 3) * this.waveHeight;
			this.top = 8;
			this.bottom = this.top + height;
		}
		
		@Override
		protected void drawStatus(MatrixStack matrices) {
			drawCenteredString(matrices, OscilscopeScreen.this.textRenderer, 
					String.format("%d gt / div", this.tickPerDiv), 
					OscilscopeScreen.this.width / 2, OscilscopeScreen.this.height - 12, 0xFFFFFFFF);
		}

		@Override
		protected void drawWaves(MatrixStack matrices, int mouseX, int mouseY) {
			long from = this.leftTick;
			long to = this.leftTick + this.hDivs * this.tickPerDiv;
			int vOffset = 0;
			List<Text> toolTip = new ArrayList<>();
			Map<Oscilscope.Channel, List<Oscilscope.Edge>> data = 
					OscilscopeScreen.this.waveData.getWaveData(from, to, true);
			for (Map.Entry<Oscilscope.Channel, List<Oscilscope.Edge>> channelPair : data.entrySet()) {
				Oscilscope.Channel channel = channelPair.getKey();
				List<Oscilscope.Edge> wave = channelPair.getValue();
				int color = this.getTransformedColor(channel);
				if (wave.isEmpty() || channel.getId() < this.topChannelId) {
					continue;
				}
				
				Oscilscope.Edge prevEdge = null;
				for (Oscilscope.Edge edge : wave) {
					if (prevEdge != null) {
						int prevX = this.calculatePosOfTime(prevEdge.time);
						int curX = this.calculatePosOfTime(edge.time);
						int prevY = this.getWaveY(prevEdge.newLevel > 0, vOffset);
						int curY = this.getWaveY(edge.newLevel > 0, vOffset);
						drawHorizontalLine(matrices, prevX, curX, prevY, color);
						drawVerticalLine(matrices, curX, prevY, curY, color);
						this.appendToolTipIfNeeded(toolTip, mouseX, mouseY, prevX, prevY, curX, curY, 
								channel, prevEdge, edge);
					}
					prevEdge = edge;
				}
				
				drawHorizontalLine(matrices, this.calculatePosOfTime(prevEdge.time), 
						this.calculatePosOfTime(ServerMicroTime.current()), 
						this.getWaveY(prevEdge.newLevel > 0, vOffset), color);
				this.appendToolTipIfNeeded(toolTip, mouseX, mouseY, 
						this.calculatePosOfTime(prevEdge.time), 
						this.getWaveY(prevEdge.newLevel > 0, vOffset), 
						this.right, -1, channel, prevEdge, null);
				drawCenteredString(matrices, OscilscopeScreen.this.textRenderer, 
						String.format("CH%d", channel.getId()), 
						this.left / 2, this.getWaveY(true, vOffset), 0xFFFFFFFF);
				if (++vOffset > this.displayedChannelCount) {
					break;
				}
			}
			
			if (!toolTip.isEmpty()) {
				OscilscopeScreen.this.renderTooltip(matrices, toolTip, mouseX, mouseY);
			}
		}
		
		private int getWaveY(boolean high, int vOffset) {
			return this.top + (vOffset * 2 - (high ? 1 : 0) + 2) * this.waveHeight;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (Screen.hasAltDown()) {
				if (Screen.hasControlDown()) {
					this.waveHeight = MathHelper.clamp(amount > 0 ? this.waveHeight * 2 : this.waveHeight / 2, 4, 16);
					this.recalculateWaveAreaParameters();
				} else {
					if (amount < 0) {
						OscilscopeScreen.this.waveData.getAllChannels().stream()
								.filter(Oscilscope.Channel::isVisible)
								.filter((ch) -> ch.getId() > this.topChannelId)
								.findFirst()
								.ifPresent((ch) -> this.topChannelId = ch.getId());
					} else {
						OscilscopeScreen.this.waveData.getAllChannels().stream()
								.filter(Oscilscope.Channel::isVisible)
								.filter((ch) -> ch.getId() < this.topChannelId)
								.reduce((ch0, ch1) -> ch1)
								.ifPresent((ch) -> this.topChannelId = ch.getId());
					}
				}
			} else {
				super.mouseScrolled(mouseX, mouseY, amount);
			}
			
			return true;
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			switch (keyCode) {
			case GLFW.GLFW_KEY_LEFT:
				if (Screen.hasControlDown()) {
					this.tickPerDiv = this.calculateNextDivLength(this.tickPerDiv, false, 1000000);
				} else {
					this.leftTick += (Screen.hasShiftDown() ? -this.hDivs : -1) * this.tickPerDiv;
				}
				
				return true;
			case GLFW.GLFW_KEY_RIGHT:
				if (Screen.hasControlDown()) {
					this.tickPerDiv = this.calculateNextDivLength(this.tickPerDiv, true, 1000000);
				} else {
					this.leftTick += (Screen.hasShiftDown() ? this.hDivs : 1) * this.tickPerDiv;
				}
				
				return true;
			default:
				return false;
			}
		}
	}
	
	private class CustomPositionEntryListWidget<E extends AlwaysSelectedEntryListWidget.Entry<E>> 
			extends AlwaysSelectedEntryListWidget<E> {
		public CustomPositionEntryListWidget(int x, int y, int height, int width, int itemHeight) {
			super(MinecraftClient.getInstance(), width, height, y, y + height, itemHeight);
			this.left = x;
			this.right = x + width;
			this.method_31323(false);
			this.method_31322(false);
		}
		
		@Override
		public int getRowLeft() {
			return this.left;
		}
		
		@Override
		public int getRowWidth() {
			return this.width;
		}
		
		@Override
		public int getScrollbarPositionX() {
			return this.right;
		}
		
		@Override
		protected void renderBackground(MatrixStack matrices) {
			DrawableHelper.fill(matrices, this.left, this.top, this.right, this.bottom, 0x7F000000);
		}
	}
	
	private final class ChannelList extends CustomPositionEntryListWidget<ChannelList.Entry> {
		private List<Text> tooltip;
		
		public ChannelList() {
			super((int) (OscilscopeScreen.this.width * 0.5) + 100, 
					(int) (OscilscopeScreen.this.height * 0.6) + 12, 
					(int) (OscilscopeScreen.this.height * 0.4) - 24, 
					(int) (OscilscopeScreen.this.width * 0.5) - 108, 
					22);
		}
		
		void newChannel(Oscilscope.Channel channel) {
			this.addEntry(new Entry(channel));
		}
		
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.tooltip = null;
			super.render(matrices, mouseX, mouseY, delta);
			if (this.tooltip != null) {
				OscilscopeScreen.this.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
			}
		}

		private final class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
			private final Oscilscope.Channel backend;
			private final ButtonWidget trigMode;
			private final TextFieldWidget trigLevel;
			private final CheckboxWidget show; 

			public Entry(Oscilscope.Channel backend) {
				this.backend = backend;
				this.trigMode = new ButtonWidget(ChannelList.this.left + 23, 1, 
						(int) (ChannelList.this.width * 0.5) - 23, 20, 
						I18N.translateAsText("oscil.gui.trigmode." + this.backend.getTrigMode()), 
						(btn) -> {
							int prevTrigModeOrd = backend.getTrigMode().ordinal();
							int newTrigModeOrd = (prevTrigModeOrd + 1) % Oscilscope.TrigMode.values().length;
							Oscilscope.TrigMode newTrigMode = Oscilscope.TrigMode.values()[newTrigModeOrd];
							this.backend.setTrigMode(newTrigMode);
							btn.setMessage(I18N.translateAsText("oscil.gui.trigmode." + newTrigMode));
						});
				this.trigLevel = new TextFieldWidget(OscilscopeScreen.this.textRenderer, 
						ChannelList.this.left + (int) (ChannelList.this.width * 0.5) + 4, 4, 
						20, 14, 
						Text.of(Integer.toString(this.backend.getTrigLevel())));
				this.trigLevel.setTextPredicate((in) -> in.matches("^[+-]?[0-9]*$"));
				this.trigLevel.setChangedListener((in) -> {
					if (!in.isEmpty()) {
						this.backend.setTrigLevel(Integer.parseInt(in));
					}
				});
				this.trigLevel.setText(Integer.toString(this.backend.getTrigLevel()));
				this.show = new CheckboxWidget(ChannelList.this.left + (int) (ChannelList.this.width * 0.5) + 28, 1, 
						(int) (ChannelList.this.width * 0.5) - 28, 20, 
						I18N.translateAsText("oscil.gui.show"), true) {
					@Override
					public void onPress() {
						super.onPress();
						Entry.this.backend.setVisible(this.isChecked());
					}
				};
			}
			
			@Override
			public int hashCode() {
				return Objects.hash(this.backend);
			}
			
			@Override
			public boolean equals(Object other) {
				return (other instanceof Entry) && this.backend.equals(((Entry) other).backend);
			}

			@Override
			public void render(MatrixStack ms, int j, int y, int x, 
					int width, int height, int mouseX, int mouseY, boolean hovering, float var10) {
				DrawableHelper.fill(ms, x + 2, y + 2, x + 19, y + 19, 0xFFFFFFFF);
				DrawableHelper.fill(ms, x + 3, y + 3, x + 18, y + 18, this.backend.getColor());
				this.trigMode.y = y + 1;
				this.trigLevel.y = y + 4;
				this.show.y = y + 1;
				this.trigMode.render(ms, mouseX, mouseY, var10);
				this.trigLevel.render(ms, mouseX, mouseY, var10);
				this.show.render(ms, mouseX, mouseY, var10);
				int deltaX = mouseX - x;
				int deltaY = mouseY - y;
				if (deltaX >= 2 && deltaX <= 19 && deltaY >= 2 && deltaY <= 19) {
					Oscilscope.Channel ch = this.backend;
					ChannelList.this.tooltip = Lists.newArrayList(
							I18N.translateAsText("oscil.gui.chinf.1", ch.getId()), 
							I18N.translateAsText("oscil.gui.chinf.2", ch.getDimensionId()), 
							I18N.translateAsText("oscil.gui.chinf.x", ch.getPos().getX()), 
							I18N.translateAsText("oscil.gui.chinf.y", ch.getPos().getY()), 
							I18N.translateAsText("oscil.gui.chinf.z", ch.getPos().getZ())
					);
				}
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return this.trigMode.mouseClicked(mouseX, mouseY, button)
						|| this.trigLevel.mouseClicked(mouseX, mouseY, button)
						|| this.show.mouseClicked(mouseX, mouseY, button);
			}
			
			@Override
			public boolean charTyped(char chr, int keyCode) {
				return this.trigLevel.charTyped(chr, keyCode);
			}
			
			@Override
		    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				return this.trigLevel.keyPressed(keyCode, scanCode, modifiers);
			}
		}
	}
	
	private final class TrigHistory extends CustomPositionEntryListWidget<TrigHistory.Entry> {
		public TrigHistory() {
			super(8, 
					(int) (OscilscopeScreen.this.height * 0.6) + 12, 
					(int) (OscilscopeScreen.this.height * 0.4) - 24, 
					(int) (OscilscopeScreen.this.width * 0.5) - 108, 
					12);
		}
		
		public void reset() {
			this.clearEntries();
		}

		void onTrig(Oscilscope.Trigger trig) {
			this.addEntry(new Entry(trig));
		}

		private final class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
			private final Oscilscope.Trigger trig;

			public Entry(Oscilscope.Trigger trig) {
				this.trig = trig;
			}

			@Override
			public void render(MatrixStack ms, int j, int y, int x, 
					int width, int height, int mouseX, int mouseY, boolean hovering, float var10) {
				MutableText text = this.trig.rising ? 
						new FormattedText("\u2B06 ", "rcl", false).asMutableText() 
						: new FormattedText("\u2B07 ", "ral", false).asMutableText();
				text.append(new FormattedText("oscil.gui.triginf", "rf", true, 
						this.trig.channel.getId(), this.trig.time.gameTime).asMutableText());
				DrawableHelper.drawTextWithShadow(ms, OscilscopeScreen.this.textRenderer, 
						text, x, y, mouseY);
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				OscilscopeScreen.this.waveArea.scrollToTrig(this.trig.time.gameTime);
				return true;
			}
		}
	}
}
