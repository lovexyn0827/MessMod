package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Inject(method = "shouldTickEntity", at = @At("HEAD"), cancellable = true)
	private void tickEntityIfNeeded(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if(((EntityInterface) entity).isFrozen()) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
