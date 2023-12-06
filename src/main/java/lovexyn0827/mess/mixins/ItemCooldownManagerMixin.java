package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;

@Mixin(ItemCooldownManager.class)
public class ItemCooldownManagerMixin {
	@Inject(method = "set", at = @At(value = "HEAD"), cancellable = true)
	private void tryCancelUsageCooldown(Item item, int duration, CallbackInfo ci) {
		if(OptionManager.disableItemUsageCooldown) {
			ci.cancel();
		}
	}
}
