package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MinecartItem;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

@Mixin(MinecartItem.class)
public abstract class MinecartItemMixin {
	@Redirect(method = "useOnBlock", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/tag/Tag;)Z"
			)
	)
	private boolean canPlaceMinecart(BlockState state, Tag<Block> tag) {
		return OptionManager.minecartPlacementOnNonRailBlocks || state.isIn(tag);
	}
	
	@ModifyVariable(method = "useOnBlock",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/block/enums/RailShape;isAscending()Z", 
					shift = At.Shift.BEFORE
					),
			index = 7
	)
	private double adjustHeight(double d, ItemUsageContext context) {
		if(OptionManager.minecartPlacementOnNonRailBlocks) {
			World world = context.getWorld();
			BlockState state = world.getBlockState(context.getBlockPos());
			double height = Math.max(state.getCollisionShape(world, context.getBlockPos()).getMax(Axis.Y), 0.0D);
			return state.isIn(BlockTags.RAILS) ? d : height - 0.0625;
		} else {
			return d;
		}
	}
}
