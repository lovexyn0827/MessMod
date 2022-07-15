package lovexyn0827.mess.rendering.hud;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.systems.RenderSystem;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

/*
 * TODO
 * Instead of the introduction of new Hud classes, it would be better to do ssomthing with the data storage.
 * It should be responable for rendering, not getting data from entities.
 */
public abstract class EntityHud {
	protected MinecraftClient client = MinecraftClient.getInstance();
	@Deprecated	// Never invoke directly
	// TODO Use a better data storage system, which is compatible with dedicated servers.
	//private Map<HudLine,Object> data = new TreeMap<>();
	private HudDataStorage data;
	public boolean shouldRender = false;
	protected int xStart;
	protected int yStart;
	private ClientHudManager hudManager;
	private int lastLineWidth = 0;
	//private List<HudLine> customLines = new ArrayList<>();
	
	public EntityHud(ClientHudManager hudManager, HudType type) {
		this.hudManager = hudManager;
		this.data = HudDataStorage.create(type);
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
		float size = OptionManager.hudTextSize;
		RenderSystem.scalef(size, size, size);
		TextRenderer tr = client.textRenderer;
		tr.drawWithShadow(ms,description,x,y, -1);
		y += 10;
		MutableInt mutableY = new MutableInt(y);
		this.getData().forEach((n, v) -> {
			if(BuiltinHudInfo.NAME.getName().equals(n) || BuiltinHudInfo.ID.getName().equals(n)) return;
			tr.drawWithShadow(ms, n + ':' + v, x, mutableY.getAndAdd(10), 0x31f38b);
		});
		this.updateAlign();
		this.hudManager.hudHeight += (mutableY.getValue() - this.yStart);
	}
	
	public void toggleRender() {
		this.shouldRender ^= true;
	}
	
	private void updateAlign() {
		AlignMode mode = OptionManager.hudAlignMode;
		this.lastLineWidth = this.getMaxLineLength();
		float size = OptionManager.hudTextSize;
		this.xStart = mode.name().contains("LEFT") ? 0 : (int) (MinecraftClient.getInstance().getWindow().getWidth() / size - this.lastLineWidth);
		int offset = this.hudManager.hudHeight;
		this.yStart = mode.name().contains("TOP") ? offset : MinecraftClient.getInstance().getWindow().getHeight()- this.getData().size() * 10 - offset;
	}
	
	@SuppressWarnings("resource")
	protected synchronized int getMaxLineLength() {
		MutableInt lineLength = new MutableInt(0);
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
