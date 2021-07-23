package mc.lovexyn0827.mcwmem.mixins;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.rendering.ShapeRenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addRenderers(MinecraftClient client, BufferBuilderStorage bufferBuilders, CallbackInfo ci)
    {
        MCWMEMod.INSTANCE.shapeRenderer = new ShapeRenderer(client);
    }

    @Inject(method = "render", at =  @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V"))
    private void renderScarpetThings(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci)
    {
        if (MCWMEMod.INSTANCE.shapeRenderer != null)
        {
            RenderSystem.pushMatrix();
            MCWMEMod.INSTANCE.shapeRenderer.render(camera, tickDelta);
            RenderSystem.popMatrix();
        }
    }
}
