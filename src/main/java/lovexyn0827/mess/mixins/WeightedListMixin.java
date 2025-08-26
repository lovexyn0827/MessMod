package lovexyn0827.mess.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.util.collection.WeightedList;

@Mixin(WeightedList.class)
public abstract class WeightedListMixin {
	@Shadow
	public abstract WeightedList<?> shuffle(Random random);
	
	@Inject(
			method = "pickRandom(Ljava/util/Random;)Ljava/lang/Object;", 
			at = @At("HEAD"), 
			cancellable = true
	)
	private void pickRandomThreadSafe(Random random, CallbackInfoReturnable<Object> cir) {
		if (OptionManager.flowerFieldRenderer) {
			synchronized (this) {
				cir.setReturnValue(this.shuffle(random).stream().findFirst().orElseThrow(RuntimeException::new));
				cir.cancel();
			}
		}
	}
}
