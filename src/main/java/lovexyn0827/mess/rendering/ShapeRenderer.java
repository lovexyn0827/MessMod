/* MIT License
 *
 * Copyright (c) 2020 gnembon
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package lovexyn0827.mess.rendering;

import com.mojang.blaze3d.systems.RenderSystem;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A modified version of carpet.script.util.ShapesRenderer
 * Original Author : gnembon
 */
public class ShapeRenderer
{
    private final Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> shapes;
    private MinecraftClient client; 
    public ShapeRenderer(MinecraftClient minecraftClient)
    {
        this.client = minecraftClient;
        shapes = new HashMap<>();
        shapes.put(World.OVERWORLD, new HashMap<>());
        shapes.put(World.NETHER, new HashMap<>());
        shapes.put(World.END, new HashMap<>());
    }

    public void render(Camera camera, float partialTick)
    {
        //Camera camera = this.client.gameRenderer.getCamera();
        ClientWorld iWorld = this.client.world;
        RegistryKey<World> dimensionType = iWorld.getRegistryKey();
        if (shapes.get(dimensionType) == null || shapes.get(dimensionType).isEmpty()) return;
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        //RenderSystem.shadeModel(7425);
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003f);
        RenderSystem.disableCull();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        // causes water to vanish
        //RenderSystem.depthMask(true);
        //RenderSystem.polygonOffset(-3f, -3f);
        //RenderSystem.enablePolygonOffset();
        //Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        // render
        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        synchronized (shapes)
        {
            this.shapes.get(dimensionType).forEach((space, set) -> {
            	set.removeIf((entry) -> entry.isExpired(MessMod.INSTANCE.getGameTime()));
            });   
            this.shapes.get(dimensionType).forEach((space, set) -> {
            	set.forEach((s) -> {
            		if(s.shouldRender(dimensionType)) {
            			s.renderFaces(tessellator, bufferBuilder, cameraX, cameraY, cameraZ, partialTick);
            			s.renderLines(tessellator, bufferBuilder, cameraX, cameraY, cameraZ, partialTick);
            		}
            	});
            });
            
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
    }

    public void addShape(Shape shape, RegistryKey<World> dim)
    {
        synchronized(this.shapes) {
        	this.shapes.get(dim).computeIfAbsent(ShapeSpace.DEFAULT, (ss) -> new HashSet<>()).add(shape);
        }
    }
    
    public void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space)
    {
        synchronized(this.shapes) {
        	this.shapes.get(dim).computeIfAbsent(space, (ss) -> new HashSet<>()).add(shape);
        }
    }
    
    public void reset()
    {
        synchronized (shapes)
        {
            shapes.values().forEach((map) -> map.values().forEach(Set::clear));
        }
    }
    
    public void clearSpace(ShapeSpace ss)
    {
    	synchronized (shapes)
        {
            shapes.values().forEach((map) -> map.computeIfAbsent(ss, (ss1) -> new HashSet<>()).clear());
        }
    }
    
    // some raw shit

    public static void drawLine(Tessellator tessellator, BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, float red1, float grn1, float blu1, float alpha) {
        builder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR); // 3
        builder.vertex(x1, y1, z1).color(red1, grn1, blu1, alpha).next();
        builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
        tessellator.draw();
        
    }

    public static void drawBoxWireGLLines(
            Tessellator tessellator, BufferBuilder builder,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            boolean xthick, boolean ythick, boolean zthick,
            float red1, float grn1, float blu1, float alpha, float red2, float grn2, float blu2)
    {
        builder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR); // 3
        if (xthick)
        {
            builder.vertex(x1, y1, z1).color(red1, grn2, blu2, alpha).next();
            builder.vertex(x2, y1, z1).color(red1, grn2, blu2, alpha).next();

            builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y2, z1).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x1, y1, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
        }
        if (ythick)
        {
            builder.vertex(x1, y1, z1).color(red2, grn1, blu2, alpha).next();
            builder.vertex(x1, y2, z1).color(red2, grn1, blu2, alpha).next();

            builder.vertex(x2, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y1, z2).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
        }
        if (zthick)
        {
            builder.vertex(x1, y1, z1).color(red2, grn2, blu1, alpha).next();
            builder.vertex(x1, y1, z2).color(red2, grn2, blu1, alpha).next();

            builder.vertex(x1, y2, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y1, z1).color(red1, grn1, blu1, alpha).next();

            builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
        }
        tessellator.draw();
    }

    public static void drawBoxFaces(
            Tessellator tessellator, BufferBuilder builder,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            boolean xthick, boolean ythick, boolean zthick,
            float red1, float grn1, float blu1, float alpha)
    {
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        if (xthick && ythick)
        {
            builder.vertex(x1, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y2, z1).color(red1, grn1, blu1, alpha).next();
            if (zthick)
            {
                builder.vertex(x1, y1, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();
            }
        }


        if (zthick && ythick)
        {
            builder.vertex(x1, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y2, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y1, z2).color(red1, grn1, blu1, alpha).next();

            if (xthick)
            {
                builder.vertex(x2, y1, z1).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();
            }
        }

        // now at least drawing one
        if (zthick && xthick)
        {
            builder.vertex(x1, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y1, z1).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x2, y1, z2).color(red1, grn1, blu1, alpha).next();
            builder.vertex(x1, y1, z2).color(red1, grn1, blu1, alpha).next();


            if (ythick)
            {
                builder.vertex(x1, y2, z1).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y2, z1).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x2, y2, z2).color(red1, grn1, blu1, alpha).next();
                builder.vertex(x1, y2, z2).color(red1, grn1, blu1, alpha).next();
            }
        }
        tessellator.draw();
    }
    
    public static class ShapeSpace {
    	/**
    	 * Default space, which can only be managed by the ShapeRenderer class automatically.
    	 */
    	public static final ShapeSpace DEFAULT = new ShapeSpace();
    	
    	@Deprecated
    	public ShapeSpace() {}
    }
}
