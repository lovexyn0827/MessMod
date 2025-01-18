package lovexyn0827.mess.mixins;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderPass;
import net.minecraft.client.render.WorldRenderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//A slight modified version of WorldRenderer_scarpetRenderMixin
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow @Final
	private DefaultFramebufferSet framebufferSet;
	
	@Inject(method = "renderParticles", 
			at = @At(
					value = "RETURN"
			)
	)
	private void renderShapes(FrameGraphBuilder frameGraphBuilder, Camera camera, LightmapTextureManager ltm,  float f, Fog fogParameters, CallbackInfo ci) {
		if (MessMod.INSTANCE.shapeRenderer != null) {
			RenderPass pass = frameGraphBuilder.createPass("messmod_shapes");
			framebufferSet.mainFramebuffer = pass.transfer(framebufferSet.mainFramebuffer);
			pass.setRenderer(() -> MessMod.INSTANCE.shapeRenderer.render(null, camera, 1.0F));
		}
	}
}
