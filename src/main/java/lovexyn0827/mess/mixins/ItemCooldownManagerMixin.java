package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ItemCooldownManager.class)
public class ItemCooldownManagerMixin {
	@Inject(method = "set(Lnet/minecraft/item/ItemStack;I)V", at = @At(value = "HEAD"), cancellable = true)
	private void tryCancelUsageCooldown(ItemStack item, int duration, CallbackInfo ci) {
		if(OptionManager.disableItemUsageCooldown) {
			ci.cancel();
		}
	}
	
	@Inject(method = "set(Lnet/minecraft/util/Identifier;I)V", at = @At(value = "HEAD"), cancellable = true)
	private void tryCancelGroupedUsageCooldown(Identifier groupId, int duration, CallbackInfo ci) {
		if(OptionManager.disableItemUsageCooldown) {
			ci.cancel();
		}
	}
}
