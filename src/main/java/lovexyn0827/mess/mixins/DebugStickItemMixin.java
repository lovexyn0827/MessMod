package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.MessMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DebugStickItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

@Mixin(DebugStickItem.class)
public abstract class DebugStickItemMixin {
	@Shadow public static BlockState cycle(BlockState state, Property<?> property, boolean inverse) {
		return null;
	}
	
	@Redirect(method = "use",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/item/DebugStickItem;cycle(Lnet/minecraft/block/BlockState;Lnet/minecraft/state/property/Property;Z)Lnet/minecraft/block/BlockState;")
			)
	public BlockState skipInvaildStatesIfNeeded(BlockState state, Property<?> property, boolean inverse,
			PlayerEntity player, BlockState stateDuplicated, WorldAccess world, BlockPos pos, boolean update, ItemStack stack) {
		if(MessMod.INSTANCE.getBooleanOption("debugStickSkipsInvaildState")) {
			Object obj = state.get(property);
			BlockState to;
			while(!(to = cycle(state, property, inverse)).canPlaceAt(world, pos)) {
				if(!obj.equals(to.get(property))) break;
			}
			
			return to;
		}
		return cycle(state, property, inverse);
	}
}
