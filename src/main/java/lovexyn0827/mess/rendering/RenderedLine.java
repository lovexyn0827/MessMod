package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Copied From The Fabric-Carpet
 */
public class RenderedLine extends Shape {

	private final Vec3d from;
	private final Vec3d to;
	
	public RenderedLine(Vec3d b, Vec3d a, int color, int life) {
		super(color, 0,life);
		this.from = a;
		this.to = b;
	}

	@Override
	protected void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
	}

	@Override
	protected void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
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

}
