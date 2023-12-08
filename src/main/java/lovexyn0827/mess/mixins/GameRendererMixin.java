package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "method_18144", 
			at = @At("HEAD"), 
			cancellable = true, 
			remap = false
	)
	private static void allowNonCollidableEntities(Entity e, CallbackInfoReturnable<Boolean> cir) {
		if(OptionManager.allowTargetingSpecialEntities) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}
}
