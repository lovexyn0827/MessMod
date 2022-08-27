package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin_Client {
	@ModifyConstant(method = "scrollInHotbar", constant = @Constant(intValue = 9))
	int modifyHotbarLength(int lengthO) {
		return OptionManager.hotbarLength;
	}
}
