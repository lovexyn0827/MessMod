package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.util.collection.WeightedList;

@Mixin(WeightedList.class)
public abstract class WeightedListMixin<T> {
	@Shadow
	public abstract WeightedList<T> shuffle();
	
	@Inject(
			method = "shuffle()Lnet/minecraft/util/collection/WeightedList;", 
			at = @At("HEAD"), 
			cancellable = true
	)
	private void pickRandomThreadSafe(CallbackInfoReturnable<WeightedList<T>> cir) {
		if (OptionManager.flowerFieldRenderer) {
			synchronized (this) {
				cir.setReturnValue(this.shuffle());
				cir.cancel();
			}
		}
	}
}
