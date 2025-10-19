package lovexyn0827.mess.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin_Server {
	@ModifyConstant(method = { "getHotbarSize", "isValidHotbarIndex" }, constant = @Constant(intValue = 9))
	private static int modifyHotbarLength(int lengthO) {
		return OptionManager.hotbarLength;
	}
	
	@Redirect(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", 
			at = @At(value = "FIELD", 
					target = "net/minecraft/entity/player/PlayerAbilities.creativeMode:Z", 
					opcode = Opcodes.GETFIELD
			))
	private boolean shouldAlwaysPickupItems(PlayerAbilities abilities) {
		return !OptionManager.disableCreativeForcePickup && abilities.creativeMode;
	}
}
