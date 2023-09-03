package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Copied From The Fabric-Carpet
 */
public class RenderedLine extends Shape {

	private final Vec3d from;
	private final Vec3d to;
	
	public RenderedLine(Vec3d b, Vec3d a, int color, int life, long gt) {
		super(color, 0, life, gt);
		this.from = a;
		this.to = b;
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderFaces(MatrixStack matrices, Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderLines(MatrixStack matrices, Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
        RenderSystem.lineWidth(2);
        ShapeRenderer.drawLine(tessellator, bufferBuilder,
                (float)(from.x-cx-renderEpsilon), (float)(from.y-cy-renderEpsilon), (float)(from.z-cz-renderEpsilon),
                (float)(to.x-cx+renderEpsilon), (float)(to.y-cy+renderEpsilon), (float)(to.z-cz+renderEpsilon),
                this.r, this.g, this.b, this.a
        );
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}
	
	@Override
	protected NbtCompound toTag(NbtCompound tag) {
		NbtCompound basic = super.toTag(tag);
		basic.putDouble("X0", this.from.x);
		basic.putDouble("Y0", this.from.y);
		basic.putDouble("Z0", this.from.z);
		basic.putDouble("X1", this.to.x);
		basic.putDouble("Y1", this.to.y);
		basic.putDouble("Z1", this.to.z);
		return basic;
	}
}
