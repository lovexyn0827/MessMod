package lovexyn0827.mess.rendering;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class Shape {
	protected static final BiMap<String, Class<? extends Shape>> IDS = HashBiMap.create();
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
	private int fill;
	
	protected Shape(int color,													 int fill, int life, long gt) {
		this.color = color;
		this.fill = fill;
		this.life = life;
		this.createdTime = gt;
        this.fr = (float)(fill >> 24 & 0xFF) / 255.0F;
        this.fg = (float)(fill >> 16 & 0xFF) / 255.0F;
        this.fb = (float)(fill >>  8 & 0xFF) / 255.0F;
        this.fa = (float)(fill & 0xFF) / 255.0F;
        this.r = (float)(color >> 24 & 0xFF) / 255.0F;
        this.g = (float)(color >> 16 & 0xFF) / 255.0F;
        this.b = (float)(color >>  8 & 0xFF) / 255.0F;
        this.a = (float)(color & 0xFF) / 255.0F;
	}

	@Environment(EnvType.CLIENT)
	protected abstract void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX,
			double cameraY, double cameraZ, float partialTick);

	@Environment(EnvType.CLIENT)
	protected abstract void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX,
			double cameraY, double cameraZ, float partialTick);

	protected abstract boolean shouldRender(RegistryKey<World> dimensionType);

	protected boolean isExpired(long gameTime) {
		return this.life + this.createdTime - gameTime < 0;
	}

	protected CompoundTag toTag(CompoundTag tag) {
		tag.putString("ID", IDS.inverse().get(this.getClass()));
		tag.putInt("Color", this.color);
		tag.putInt("Fill", this.fill);
		tag.putInt("Life", this.life);
		tag.putLong("GT", this.createdTime);
		return tag;
	}
	
	protected void close() {
	}

	public static Shape fromTag(CompoundTag tag) {
		switch(tag.get("ID").asString()) {
		case "box" : 
			return new RenderedBox(tag.getDouble("X0"), tag.getDouble("Y0"), tag.getDouble("Z0"), 
					tag.getDouble("X1"), tag.getDouble("Y1"), tag.getDouble("Z1"), 
					tag.getInt("Color"), tag.getInt("Fill"), tag.getInt("Life"), tag.getLong("GT"));
		case "line" : 
			return new RenderedLine(new Vec3d(tag.getDouble("X0"), tag.getDouble("Y0"), tag.getDouble("Z0")), 
					new Vec3d(tag.getDouble("X1"), tag.getDouble("Y1"), tag.getDouble("Z1")), 
					tag.getInt("Color"), tag.getInt("Life"), tag.getLong("GT"));
		case "text" : 
			return new RenderedText(tag.getString("Value"), 
					new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")), 
							tag.getInt("Color"), tag.getInt("Life"), tag.getLong("GT"));
		case "bitmap" : 
			// TODO GZIP
			return new RenderedText(tag.getString("Value"), 
					new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")), 
							tag.getInt("Color"), tag.getInt("Life"), tag.getLong("GT"));
		}
		
		return null;
	}
	
	static {
		IDS.put("box", RenderedBox.class);
		IDS.put("line", RenderedLine.class);
		IDS.put("text", RenderedText.class);
	}
}
