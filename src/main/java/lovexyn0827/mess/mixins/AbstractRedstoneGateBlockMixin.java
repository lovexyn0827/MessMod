package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(AbstractRedstoneGateBlock.class)
public interface AbstractRedstoneGateBlockMixin {
	@Invoker("getOutputLevel")
	int getOutputRSLevel(BlockView world, BlockPos pos, BlockState state);
}
