package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
	private boolean firstFrame;
	
	@Inject(method = "render", at = @At("HEAD"))
	public void renderMore(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, 
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.firstFrame) {
			this.firstFrame = false;
		}
	
		if(OptionManager.vanillaDebugRenderers != null) {
			OptionManager.vanillaDebugRenderers.forEach((renderer) -> {
				try {
					((DebugRenderer.Renderer) renderer.left().get().get(this))
							.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
}
