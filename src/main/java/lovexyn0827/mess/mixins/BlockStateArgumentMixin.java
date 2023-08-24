package lovexyn0827.mess.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockStateArgument.class)
public class BlockStateArgumentMixin {
	@Nullable @Shadow @Final
	private NbtCompound data;
	
	@Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld."
			+ "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), 
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void onSetBlock(ServerWorld serverWorld, BlockPos blockPos, int i, CallbackInfoReturnable<Boolean> cir, 
			BlockState blockState) {
		if(OptionManager.fillHistory) {
			BlockEntity be = serverWorld.getBlockEntity(blockPos);
			NbtCompound tag;
			if(this.data != null) {
				tag = data.copy();
				tag.putInt("x", blockPos.getX());
				tag.putInt("y", blockPos.getY());
				tag.putInt("z", blockPos.getZ());
			} else {
				tag = null;
			}
			
			BlockPlacementHistory.appendBlockChange(serverWorld, blockPos, serverWorld.getBlockState(blockPos), 
					blockState, be == null ? null : be.createNbtWithIdentifyingData(), tag);
		}
	}
}
