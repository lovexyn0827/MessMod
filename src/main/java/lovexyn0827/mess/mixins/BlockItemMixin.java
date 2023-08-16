package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", 
			at = @At(value = "INVOKE", 
							target = "Lnet/minecraft/item/BlockItem;place"
									+ "(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z"
							), 
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void recordPlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, 
			ItemPlacementContext itemPlacementContext, BlockState blockState) {
		PlayerEntity player = context.getPlayer();
		if(OptionManager.blockPlacementHistory && context.getPlayer() instanceof ServerPlayerEntity) {
			BlockPlacementHistory history = ((ServerPlayerEntityInterface) player).getBlockPlacementHistory();
			if(history != null) {
				history.pushSingle(context.getBlockPos(), player.world.getBlockState(context.getBlockPos()), 
						blockState, player.world.getBlockEntity(context.getBlockPos()));
			}
		}
	}
}
