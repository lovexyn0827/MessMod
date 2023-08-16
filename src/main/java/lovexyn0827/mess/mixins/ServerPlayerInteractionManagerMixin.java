package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(method = "tryBreakBlock", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/block/Block;onBroken"
							+ "(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"
					), 
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void recordBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> ci, BlockState blockState, 
			BlockEntity blockEntity, Block block, boolean bl) {
		if(OptionManager.blockPlacementHistory) {
			BlockPlacementHistory history = ((ServerPlayerEntityInterface) this.player).getBlockPlacementHistory();
			if(history != null) {
				history.pushSingle(pos, blockState, Blocks.AIR.getDefaultState(), blockEntity);
			}
		}
	}
}
