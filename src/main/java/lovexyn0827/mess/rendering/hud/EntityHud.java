package lovexyn0827.mess.rendering.hud;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.network.MessModPayload;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;

/**
 * It should be responsible for rendering, not getting data from entities.
 */
public abstract class EntityHud {
	protected MinecraftClient client = MinecraftClient.getInstance();
	@Deprecated	// Never use it directly
	private HudDataStorage data;
	public boolean shouldRender = false;
	protected int xStart;
	protected int yStart;
	private ClientHudManager hudManager;
	private int lastLineWidth = 0;
	private int xEnd;
	private HudType type;
	
	public EntityHud(ClientHudManager hudManager, HudType type) {
		this.hudManager = hudManager;
		this.data = this.createDataStorage(type);
		this.type = type;
	}
	
	protected HudDataStorage createDataStorage(HudType type) {
		return HudDataStorage.create(type);
	}

	public synchronized void render(String description) {
		int y = this.yStart;
		int x = this.xStart;
		// i don't know how it works, but it runs correctly...
		RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		Matrix4f matrix4f = new Matrix4f().setOrtho(0.0f, this.client.getWindow().getFramebufferWidth(), 
				this.client.getWindow().getFramebufferHeight(), 0.0f, 1000.0f, 3000.0f);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.loadIdentity();
		matrixStack.translate(0.0f, 0.0f, -2000.0f);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.lineWidth(1.0f);
		RenderSystem.disableBlend();
		TextRenderer tr = client.textRenderer;
		this.updateAlign(tr.getWidth(description));
		DrawContext dc = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
		float size = OptionManager.hudTextSize;
		dc.getMatrices().scale(size, size, size);
		ClientHudManager chm = MessMod.INSTANCE.getClientHudManager();
		dc.drawTextWithShadow(tr, description, x, y, 0xFFFFFF);
		y += 10;
		MutableInt mutableY = new MutableInt(y);
		MutableBoolean darkBg = new MutableBoolean(true);
		this.getData().forEach((n, v) -> {
			if(BuiltinHudInfo.NAME.getName().equals(n) || BuiltinHudInfo.ID.getName().equals(n)) return;
			String header = n + ':';
			String data = v == null ? "null" : v.toString();
			int y0 = mutableY.intValue();
			if(chm.renderBackGround) {
				dc.fill(x, y0, this.xEnd, y0 + 10, darkBg.booleanValue() ? 0x80000000 : 0x80808080);
				darkBg.setValue(!darkBg.getValue());
			}
			
			int dataX = chm.looserLines ? 
					(int) (MinecraftClient.getInstance().getWindow().getWidth() / size) - tr.getWidth(data) : x + tr.getWidth(header);
			dc.drawTextWithShadow(tr, header, x, y0, chm.headerSpeciallyColored ? 0xFF4040 : 0x31F38B);
			dc.drawTextWithShadow(tr, data, dataX, mutableY.getAndAdd(10), 0x31F38B);
			
		});
		this.hudManager.hudHeight += (mutableY.getValue() - this.yStart);
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	public void toggleRender() {
		this.shouldRender ^= true;
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeEnumConstant(this.type);
		buf.writeBoolean(this.shouldRender);
		CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(new MessModPayload(Channels.HUD, buf));
		MessMod.INSTANCE.getClientNetworkHandler().send(packet);
	}
	
	private int calculateHeight() {
		int[] height = new int[1];
		this.getData().forEach((n, o) -> {
			if(!(BuiltinHudInfo.NAME.getName().equals(n) || BuiltinHudInfo.ID.getName().equals(n))) {
				height[0] += 10;
			}
		});
		
		return height[0] + 10;
	}
	
	private void updateAlign(int headerLineWidth) {
		AlignMode mode = OptionManager.hudAlignMode;
		this.lastLineWidth = this.getMaxLineLength(headerLineWidth);
		float size = OptionManager.hudTextSize;
		boolean left = mode.name().contains("LEFT");
		int windowWidth = MinecraftClient.getInstance().getWindow().getWidth();
		this.xStart = left ? 0 : (int) (windowWidth / size - this.lastLineWidth + 1);
		this.xEnd = left ? this.lastLineWidth - 1 : (int) (windowWidth / size);
		int offset = this.hudManager.hudHeight;
		int windowHeight = (int) (MinecraftClient.getInstance().getWindow().getHeight() / size);
		this.yStart = mode.name().contains("TOP") ? offset : windowHeight - this.calculateHeight() - offset;
	}
	
	@SuppressWarnings("resource")
	protected synchronized int getMaxLineLength(int headerLineWidth) {
		MutableInt lineLength = new MutableInt(headerLineWidth);
		this.getData().forEach((n, v) -> {
			TextRenderer tr = MinecraftClient.getInstance().textRenderer;
			lineLength.setValue(Math.max(lineLength.getValue(), tr.getWidth(n + ':' + v)));;
		});
		return OptionManager.stableHudLocation ? Math.max(lineLength.getValue(), this.lastLineWidth) : lineLength.getValue();
	}

	protected final synchronized HudDataStorage getData() {
		return this.data;
	}
}
