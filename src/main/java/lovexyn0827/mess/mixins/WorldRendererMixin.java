package lovexyn0827.mess.mixins;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//A slight modified version of WorldRenderer_scarpetRenderMixin
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "render", at =  @At(
    		value = "INVOKE",
    		target = "Lnet/minecraft/client/render/TexturedRenderLayers;getEntityTranslucentCull()Lnet/minecraft/client/render/RenderLayer;", 
    		shift = At.Shift.BEFORE
    ))
	private void renderShapes(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, 
			GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f viewMat, 
			Matrix4f matrix4f, CallbackInfo ci) {
		MessMod.INSTANCE.shapeRenderer.render(viewMat, camera, 1.0F);
	}
}
