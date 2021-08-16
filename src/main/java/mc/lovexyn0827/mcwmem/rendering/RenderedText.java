package mc.lovexyn0827.mcwmem.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.AffineTransformation;
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

	public RenderedText(String value, Vec3d pos, int color, int life) {
		super(color, 0x0000002f,life);
		this.value = value;
		this.pos = pos;
	}

	@Override
	protected void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
	}

	@Override
	protected void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
		if (this.a == 0.0) return;
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
        RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(this.value, 0, 0.0F, this.color, false, AffineTransformation.identity().getMatrix(), immediate, false, 0x0000002f, 15728880);
        immediate.draw();
        RenderSystem.popMatrix();
        RenderSystem.enableCull();
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}
}
