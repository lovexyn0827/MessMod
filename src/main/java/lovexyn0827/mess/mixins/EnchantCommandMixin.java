package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.server.command.EnchantCommand;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {
	// NOTE we can use MixinExtra features as we want since MC1.20.6 is releases after it is included in Fabric Loader.
	@ModifyExpressionValue(
			method = "execute", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/enchantment/Enchantment.getMaxLevel()I"
			)
	)
	private static int modifyMaxLevel(int i) {
		if(OptionManager.disableEnchantCommandRestriction) {
			return 2147483647;
		} else {
			return i;
		}
	}
	
	@ModifyExpressionValue(
			method = "execute", 
			at = {
					@At(
							value = "INVOKE", 
							target = "net/minecraft/enchantment/Enchantment.isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/enchantment/EnchantmentHelper"
									+ ".isCompatible(Ljava/lang/Collection;Lnet/minecraft/enchantment/Enchantmen;)Z"
					)
			}
	)
	private static boolean redirectItemValidation(boolean bl) {
		if(OptionManager.disableEnchantCommandRestriction) {
			return true;
		} else {
			return bl;
		}
	}
}
