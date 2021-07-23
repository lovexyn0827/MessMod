package mc.lovexyn0827.mcwmem.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * A modified version of carpet.script.util.ShapesRenderer
 * Original Author : Gnembon
 */
public class ShapeRenderer
{
    private final Map<RegistryKey<World>, List<Shape>> shapes;
    private MinecraftClient client; 
    public ShapeRenderer(MinecraftClient minecraftClient)
    {
        this.client = minecraftClient;
        shapes = new HashMap<>();
        shapes.put(World.OVERWORLD, new ArrayList<>());
        shapes.put(World.NETHER, new ArrayList<>());
        shapes.put(World.END, new ArrayList<>());
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
            shapes.get(dimensionType).removeIf(
                    entry -> entry.isExpired(MCWMEMod.INSTANCE.getGameTime())
            );

            shapes.get(dimensionType).forEach(
                    s ->
                    {
                        if (s.shouldRender(dimensionType))
                            s.renderFaces(tessellator, bufferBuilder, cameraX, cameraY, cameraZ, partialTick);
                    }
            );
            //lines
            shapes.get(dimensionType).forEach(
            		

                    s -> {
                        if (s.shouldRender(dimensionType))
                            s.renderLines(tessellator, bufferBuilder, cameraX, cameraY, cameraZ, partialTick);
                    }
            );
            shapes.get(dimensionType).forEach(
                    s ->
                    {
                        if (s.shouldRender(dimensionType))
                            s.renderLines(tessellator, bufferBuilder, cameraX, cameraY, cameraZ, partialTick);
                    }
            );

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
        	this.shapes.get(dim).add(shape);
        }
    }
    public void reset()
    {
        synchronized (shapes)
        {
            shapes.values().forEach(Collection::clear);
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
}
