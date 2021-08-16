package mc.lovexyn0827.mcwmem.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Copied From The Fabric-Carpet
 */
public class RenderedBox extends Shape {

	private Box box;

	public RenderedBox(Box box, int color, int fill, int life) {
		super(color, fill, life);
		this.box = box;
	}
	
	public RenderedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
			int color, int fill, int life) {
		super(color, fill, life);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void renderFaces(Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
		if ((this.a) == 0) return;
        RenderSystem.lineWidth(1.0F);
        ShapeRenderer.drawBoxFaces(tessellator, bufferBuilder,
                (float)(box.minX-cx-renderEpsilon), (float)(box.minY-cy-renderEpsilon), (float)(box.minZ-cz-renderEpsilon),
                (float)(box.maxX-cx+renderEpsilon), (float)(box.maxY-cy+renderEpsilon), (float)(box.maxZ-cz+renderEpsilon),
                box.minX!=box.maxX, box.minY!=box.maxY, box.minZ!=box.maxZ,
                this.fr, this.fg, this.fb, this.fa
        );
	}

	@Override
	protected void renderLines(Tessellator tessellator, BufferBuilder bufferBuilder, double cx, double cy,
			double cz, float partialTick) {
		if (this.a == 0.0) return;
        RenderSystem.lineWidth(2);
        ShapeRenderer.drawBoxWireGLLines(tessellator, bufferBuilder,
                (float)(box.minX-cx-renderEpsilon), (float)(box.minY-cy-renderEpsilon), (float)(box.minZ-cz-renderEpsilon),
                (float)(box.maxX-cx+renderEpsilon), (float)(box.maxY-cy+renderEpsilon), (float)(box.maxZ-cz+renderEpsilon),
                box.minX!=box.maxX, box.minY!=box.maxY, box.minZ!=box.maxZ,
                this.r, this.g, this.b, this.a, this.r, this.g, this.b
        );
	}

	@Override
	protected boolean shouldRender(RegistryKey<World> dimensionType) {
		// TODO Auto-generated method stub
		return true;
	}

}
