package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class Shape {
	public final int color;
	public final int life;
	protected float r;
	protected float g;
	protected float b;
	protected float a;
	protected float fr;
	protected float fg;
	protected float fb;
	protected float fa;
	private long createdTime;
	double renderEpsilon = 0;
    protected MinecraftClient client = MinecraftClient.getInstance();
	
	protected Shape(int color, int fill, int life) {
		this.color = color;
		this.life = life;
		this.createdTime = MessMod.INSTANCE.getGameTime();
        this.fr = (float)(fill >> 24 & 0xFF) / 255.0F;
        this.fg = (float)(fill >> 16 & 0xFF) / 255.0F;
        this.fb = (float)(fill >>  8 & 0xFF) / 255.0F;
        this.fa = (float)(fill & 0xFF) / 255.0F;
        this.r = (float)(color >> 24 & 0xFF) / 255.0F;
        this.g = (float)(color >> 16 & 0xFF) / 255.0F;
        this.b = (float)(color >>  8 & 0xFF) / 255.0F;
        this.a = (float)(color & 0xFF) / 255.0F;
	}
	
	protected abstract void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX,
			double cameraY, double cameraZ, float partialTick);
	
	protected abstract void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX,
			double cameraY, double cameraZ, float partialTick);

	protected abstract boolean shouldRender(RegistryKey<World> dimensionType);

	protected boolean isExpired(long gameTime) {
		return this.life + this.createdTime - gameTime < 0;
	}
}
