package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin_Server {
	@ModifyConstant(method = { "getHotbarSize", "isValidHotbarIndex" }, constant = @Constant(intValue = 9))
	private static int modifyHotbarLength(int lengthO) {
		return OptionManager.hotbarLength;
	}
}
