package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Copied From The Fabric-Carpet
 */
public class RenderedBox extends Shape {

	private Box box;

	public RenderedBox(Box box, int color, int fill, int life, long gt) {
		super(color, fill, life, gt);
		this.box = box;
	}
	
	public RenderedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
			int color, int fill, int life, long gt) {
		super(color, fill, life, gt);
		this.box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderFaces(MatrixStack matrices, Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
		if ((this.a) == 0) return;
        RenderSystem.lineWidth(1.0F);
        ShapeRenderer.drawBoxFaces(tessellator, bufferBuilder,
                (float)(box.minX - cx - renderEpsilon), (float)(box.minY - cy - renderEpsilon), (float)(box.minZ - cz - renderEpsilon),
                (float)(box.maxX - cx + renderEpsilon), (float)(box.maxY - cy + renderEpsilon), (float)(box.maxZ - cz + renderEpsilon),
                box.minX != box.maxX, box.minY != box.maxY, box.minZ != box.maxZ,
                this.fr, this.fg, this.fb, this.fa
        );
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void renderLines(MatrixStack matrices, Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
		if (this.a == 0.0) return;
        RenderSystem.lineWidth(2.0F);
        ShapeRenderer.drawBoxWireGLLines(tessellator, bufferBuilder,
                (float)(box.minX - cx - renderEpsilon), (float)(box.minY - cy - renderEpsilon), (float)(box.minZ - cz - renderEpsilon),
                (float)(box.maxX - cx + renderEpsilon), (float)(box.maxY - cy + renderEpsilon), (float)(box.maxZ - cz + renderEpsilon),
                box.minX != box.maxX, box.minY != box.maxY, box.minZ != box.maxZ,
                this.r, this.g, this.b, this.a, this.r, this.g, this.b
        );
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		return true;
	}

	@Override
	protected NbtCompound toTag(NbtCompound tag) {
		NbtCompound basic = super.toTag(tag);
		basic.putDouble("X0", this.box.minX);
		basic.putDouble("Y0", this.box.minY);
		basic.putDouble("Z0", this.box.minZ);
		basic.putDouble("X1", this.box.maxX);
		basic.putDouble("Y1", this.box.maxY);
		basic.putDouble("Z1", this.box.maxZ);
		return basic;
	}
}
