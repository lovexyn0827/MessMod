package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.util.math.BlockPos;

@Mixin(RailPlacementHelper.class)
public abstract class RailPlacementHelperMixin {
	@Inject(method = "canConnect(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"))
	private void cancelIfNeeded(BlockPos bp, CallbackInfoReturnable<Boolean> cir) {
		if(OptionManager.railNoAutoConnection) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
