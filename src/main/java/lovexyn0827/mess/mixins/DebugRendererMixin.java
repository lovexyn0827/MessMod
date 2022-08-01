package lovexyn0827.mess.mixins;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.fakes.DebugRendererEnableState;
import lovexyn0827.mess.options.InvaildOptionException;
import lovexyn0827.mess.options.ListParser;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin implements DebugRendererEnableState {
	private List<DebugRenderer.Renderer> enabledRenderers = Collections.emptyList();
	private boolean firstFrame;
	
	@Inject(method = "render", at = @At("HEAD"))
	public void renderMore(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, 
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (this.firstFrame) {
			this.update();
			this.firstFrame = false;
		}
	
		this.enabledRenderers.forEach((renderer) -> renderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ));
	}

	@Override
	public void setEnabledRenderers(List<DebugRenderer.Renderer> renderers) {
		this.enabledRenderers = renderers;
	}
	
	@Override
	public void update() {
		try {
			this.enabledRenderers = new ListParser.DebugRender().tryParse(OptionManager.vanillaDebugRenderers);
		} catch (InvaildOptionException e) {
			this.enabledRenderers = Collections.emptyList();
		}
	}
}
