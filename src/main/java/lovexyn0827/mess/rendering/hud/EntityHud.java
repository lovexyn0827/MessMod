package lovexyn0827.mess.rendering.hud;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.systems.RenderSystem;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

/**
 * It should be responable for rendering, not getting data from entities.
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

	public synchronized void render(MatrixStack ms, String description) {
		int y = this.yStart;
		int x = this.xStart;
		// i don't know how it works, but it runs correctly...
		RenderSystem.matrixMode(5889);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0D, (double)this.client.getWindow().getFramebufferWidth(), (double)this.client.getWindow().getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
		RenderSystem.matrixMode(5888);
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
		TextRenderer tr = this.client.textRenderer;
		this.updateAlign(tr.getWidth(description));
		float size = OptionManager.hudTextSize;
		RenderSystem.scalef(size, size, size);
		ClientHudManager chm = MessMod.INSTANCE.getClientHudManager();
		tr.drawWithShadow(ms, description, x, y, -1);
		y += 10;
		MutableInt mutableY = new MutableInt(y);
		MutableBoolean darkBg = new MutableBoolean(true);
		this.getData().forEach((n, v) -> {
			if(BuiltinHudInfo.NAME.getName().equals(n) || BuiltinHudInfo.ID.getName().equals(n)) return;
			String header = n + ':';
			String data = v.toString();
			int y0 = mutableY.intValue();
			if(chm.renderBackGround) {
				DrawableHelper.fill(ms, x, y0, this.xEnd, y0 + 10, darkBg.booleanValue() ? 0x80000000 : 0x80808080);
				darkBg.setValue(!darkBg.getValue());
			}
			
			int dataX = chm.looserLines ? 
					this.xEnd - tr.getWidth(data) : x + tr.getWidth(header);
			tr.drawWithShadow(ms, header, x, y0, chm.headerSpeciallyColored ? 0xFF4040 : 0x31F38B);
			tr.drawWithShadow(ms, data, dataX, mutableY.getAndAdd(10), 0x31F38B);
			
		});
		this.hudManager.hudHeight += mutableY.getValue() - this.yStart;
	}
	
	public void toggleRender() {
		this.shouldRender ^= true;
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeEnumConstant(this.type);
		buf.writeBoolean(this.shouldRender);
		CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(Channels.HUD, buf);
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
