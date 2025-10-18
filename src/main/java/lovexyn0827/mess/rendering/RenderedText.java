 package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Copied From The Fabric-Carpet
 */
public class RenderedText extends Shape {

	private String value;
	private Vec3d pos;
	private float scale;

	public RenderedText(String value, Vec3d pos, int color, int life, long gt) {
		super(color, 0x0000002f, life, gt);
		this.value = value;
		this.pos = pos;
	}

	public RenderedText(String value, Vec3d pos, int color, float scale, int life, long gt) {
		super(color, 0x0000002f, life, gt);
		this.value = value;
		this.pos = pos;
		this.scale = scale;
	}

	@Override
	protected void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
		if (this.a == 0.0) return;
		MinecraftClient client = MinecraftClient.getInstance();
		Camera camera1;
        if(client.gameRenderer != null) {
        	camera1 = client.gameRenderer.getCamera();
        } else {
        	return;
        }
        
        
        TextRenderer textRenderer = client.textRenderer;
        double d = camera1.getPos().x;
        double e = camera1.getPos().y;
        double f = camera1.getPos().z;
        RenderSystem.disableCull();
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(pos.x - d), (float)(pos.y - e), (float)(pos.z - f));
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);

        RenderSystem.multMatrix(new Matrix4f(camera1.getRotation()));
        RenderSystem.scalef(0.01f, -0.01f, 0.01f);
        RenderSystem.scalef(-this.scale, this.scale, this.scale);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        int argb = ((int) (this.a * 256) << 24) | (this.color >> 8);
        textRenderer.draw(this.value, 0, 0.0F, argb, false, AffineTransformation.identity().getMatrix(), immediate, false, 0x0000002f, 15728880);
        immediate.draw();
        RenderSystem.popMatrix();
        RenderSystem.enableCull();
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}
	
	@Override
	protected CompoundTag toTag(CompoundTag tag) {
		CompoundTag basic = super.toTag(tag);
		basic.putDouble("X", this.pos.x);
		basic.putDouble("Y", this.pos.y);
		basic.putDouble("Z", this.pos.z);
		basic.put("Value", StringTag.of(this.value));
		basic.put("Scale", FloatTag.of(this.scale));
		return basic;
	}
}
