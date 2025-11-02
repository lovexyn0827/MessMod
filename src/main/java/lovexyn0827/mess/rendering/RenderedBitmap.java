package lovexyn0827.mess.rendering;

import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
			im.setColorArgb(i % this.width, i / this.width, this.pixels[i]);
		}
		
		TextureManager tm = MinecraftClient.getInstance().getTextureManager();
		Identifier textureId = Identifier.of("messmod", "messmod_bitmap_" + NEXT_ID.getAndIncrement());
		tm.registerTexture(textureId, texture);
		texture.upload();
		this.texture = texture;
		this.layer = RenderLayer.getText(textureId);
	}
	
	@Override
	protected void renderFaces(MatrixStack matrices, Tessellator tessellator, 
			double cx, double cy, double cz, float partialTick) {
		if (!this.uploaded) {
			this.uploadTexture();
			this.uploaded = true;
		}
		
		//GL32.glAlphaFunc(GL11.GL_ALWAYS, 0);
		this.texture.bindTexture();
		//MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
		BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		float x0 = (float) (this.origin.x - cx);
		float y0 = (float) (this.origin.y - cy);
		float z0 = (float) (this.origin.z - cz);
		float width = (float) (this.width * this.pixelSize);
		float height = (float) (this.height * this.pixelSize);
		switch (this.oriention) {
		case X:
			builder.vertex(x0, y0, z0).texture(0, 0);
			builder.vertex(x0, y0, z0 + width).texture(0, 1);
			builder.vertex(x0, y0 + height, z0 + width).texture(1, 1);
			builder.vertex(x0, y0 + height, z0).texture(1, 0);
			break;
		case Y:
			builder.vertex(x0, y0, z0).texture(0, 0);
			builder.vertex(x0, y0, z0 + height).texture(0, 1);
			builder.vertex(x0 + width, y0, z0 + height).texture(1, 1);
			builder.vertex(x0 + width, y0, z0).texture(1, 0);
			break;
		case Z:
			builder.vertex(x0, y0, z0).texture(0, 0);
			builder.vertex(x0, y0, z0 + width).texture(0, 1);
			builder.vertex(x0, y0 + height, z0 + width).texture(1, 1);
			builder.vertex(x0, y0 + height, z0).texture(1, 0);
			break;
		}
		
		BufferRenderer.draw(builder.end());
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}

	@Override
	protected NbtCompound toTag(NbtCompound tag) {
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
	
	public static RenderedBitmap fromTag(NbtCompound tag) {
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

	@Override
	protected void renderLines(MatrixStack matrices, Tessellator tessellator, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
	}
}
