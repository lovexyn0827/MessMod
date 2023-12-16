package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import lovexyn0827.mess.command.SetExplosionBlockCommand;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
	@ModifyArg(method = "onExploded",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState"
							+ "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
			index = 1)
	private BlockState replaceToCustomBlockState(BlockState blockState) {
		BlockState customBlockState = SetExplosionBlockCommand.getBlockState();
		return customBlockState == null ? blockState : customBlockState;
	}
}
