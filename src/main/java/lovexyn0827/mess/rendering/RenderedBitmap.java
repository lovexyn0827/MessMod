package lovexyn0827.mess.rendering;

import java.util.concurrent.atomic.AtomicInteger;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class RenderedBitmap extends Shape {
	private static final AtomicInteger NEXT_ID = new AtomicInteger();
	private final int[] pixels;
	private final double pixelSize;
	private final Direction.Axis oriention;
	private final Vec3d origin;
	private final int height;
	private final int width;
	
	@Environment(EnvType.CLIENT)
	private NativeImageBackedTexture texture;
	
	@Environment(EnvType.CLIENT)
	private RenderLayer layer;
	
	@Environment(EnvType.CLIENT)
	private boolean uploaded = false;

	public RenderedBitmap(int[] pixels, double pixelSize, int height, int width,
			Direction.Axis oriention, Vec3d origin, int life, long gt) {
		super(0 /* Unused */, 0 /* Unused */, life, gt);
		this.height = height;
		this.width = width;
		this.pixels = pixels;
		this.pixelSize = pixelSize;
		this.oriention = oriention;
		this.origin = origin;
	}

	@Environment(EnvType.CLIENT)
	private void uploadTexture() {
		int pixelCnt = this.width * this.height;
		NativeImageBackedTexture texture = new NativeImageBackedTexture(this.width, this.height, true);
		NativeImage im = texture.getImage();
		for (int i = 0; i < pixelCnt; i++) {
			im.setPixelColor(i % this.width, i / this.width, this.pixels[i]);
		}
		
		TextureManager tm = MinecraftClient.getInstance().getTextureManager();
		Identifier textureId = tm.registerDynamicTexture("messmod_bitmap_" + NEXT_ID.getAndIncrement(), texture);
		texture.upload();
		this.texture = texture;
		this.layer = RenderLayer.getText(textureId);
	}
	
	@Override
	protected void renderFaces(Tessellator tessellator, BufferBuilder builder, double cx, double cy, double cz, 
			float partialTick) {
		if (!this.uploaded) {
			this.uploadTexture();
			this.uploaded = true;
		}
		
		RenderSystem.enableTexture();
		RenderSystem.alphaFunc(GL11.GL_ALWAYS, 0);
		this.texture.bindTexture();
		//MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		double x0 = this.origin.x - cx;
		double y0 = this.origin.y - cy;
		double z0 = this.origin.z - cz;
		double width = this.width * this.pixelSize;
		double height = this.height * this.pixelSize;
		switch (this.oriention) {
		case X:
			builder.vertex(x0, y0, z0).texture(0, 0).next();
			builder.vertex(x0, y0, z0 + width).texture(0, 1).next();
			builder.vertex(x0, y0 + height, z0 + width).texture(1, 1).next();
			builder.vertex(x0, y0 + height, z0).texture(1, 0).next();
			break;
		case Y:
			builder.vertex(x0, y0, z0).texture(0, 0).next();
			builder.vertex(x0, y0, z0 + height).texture(0, 1).next();
			builder.vertex(x0 + width, y0, z0 + height).texture(1, 1).next();
			builder.vertex(x0 + width, y0, z0).texture(1, 0).next();
			break;
		case Z:
			builder.vertex(x0, y0, z0).texture(0, 0).next();
			builder.vertex(x0, y0, z0 + width).texture(0, 1).next();
			builder.vertex(x0, y0 + height, z0 + width).texture(1, 1).next();
			builder.vertex(x0, y0 + height, z0).texture(1, 0).next();
			break;
		}
		
		tessellator.draw();
		RenderSystem.disableTexture();
	}

	@Override
	protected void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}

	@Override
	protected CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		tag.putDouble("PixelSize", this.pixelSize);
		tag.putDouble("X", this.origin.x);
		tag.putDouble("Y", this.origin.y);
		tag.putDouble("Z", this.origin.z);
		tag.putInt("Oriention", this.oriention.ordinal());
		tag.putInt("Width", this.width);
		tag.putInt("Height", this.height);
		tag.putIntArray("Pixels", this.pixels);
		return tag;
	}
	
	public static RenderedBitmap fromTag(CompoundTag tag) {
		return new RenderedBitmap(tag.getIntArray("Pixels"), tag.getDouble("PixelSize"), 
				tag.getInt("Height"), tag.getInt("Width"), 
				Direction.Axis.values()[tag.getInt("Oriention")], 
				new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")), 
				tag.getInt("Life"), tag.getLong("GT"));
	}
	
	@Override
	protected void close() {
		if (this.texture != null) {
			this.texture.close();
		}
	}
}
