package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.EnchantCommand;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
	@Redirect(
			method = "execute", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/enchantment/Enchantment.getMaxLevel()I"
			)
	)
	private static int modifyMaxLevel(Enchantment e) {
		if(OptionManager.disableEnchantCommandRestriction) {
			return 2147483647;
		} else {
			return e.getMaxLevel();
		}
	}
	
	@Redirect(
			method = "execute", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/enchantment/Enchantment.isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"
			)
	)
	private static boolean redirectItemValidation(Enchantment e, ItemStack stack) {
		if(OptionManager.disableEnchantCommandRestriction) {
			return true;
		} else {
			return e.isAcceptableItem(stack);
		}
	}
}
