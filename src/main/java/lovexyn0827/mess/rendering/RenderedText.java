 package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
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
		this.scale = 1.0F;
	}

	public RenderedText(String value, Vec3d pos, int color, float scale, int life, long gt) {
		super(color, 0x0000002f, life, gt);
		this.value = value;
		this.pos = pos;
		this.scale = scale;
	}

	@Override
	protected void renderFaces(MatrixStack matrices, Tessellator tessellator, double cameraX, double cameraY,
			double cameraZ, float partialTick) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderLines(MatrixStack matrices, Tessellator tessellator, double cameraX, double cameraY,
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
        matrices.push();
        matrices.translate((float)(pos.x - d), (float)(pos.y - e), (float)(pos.z - f));
        matrices.multiply(camera1.getRotation());
        matrices.scale(0.01f, -0.01f, 0.01f);
        matrices.scale(-this.scale, this.scale, this.scale);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferAllocator(RenderLayer.DEFAULT_BUFFER_SIZE));
        //textRenderer.draw(this.value, 0, 0.0F, this.color, false, matrices.peek().getModel(), immediate, false, 0x0000002f, 15728880);
        textRenderer.draw(this.value, 0, 0, this.color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0x0000002f, 15728880);
        immediate.draw();
        matrices.pop();
        RenderSystem.enableCull();
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}
	
	@Override
	protected NbtCompound toTag(NbtCompound tag) {
		NbtCompound basic = super.toTag(tag);
		basic.putDouble("X", this.pos.x);
		basic.putDouble("Y", this.pos.y);
		basic.putDouble("Z", this.pos.z);
		basic.put("Value", NbtString.of(this.value));
		basic.put("Scale", NbtFloat.of(this.scale));
		return basic;
	}
}
