package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.electronic.WaveGenerator;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LoomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(LoomBlock.class)
public abstract class LoomBlockMixin extends AbstractBlock {
	public LoomBlockMixin(Settings settings) {
		super(settings);
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return OptionManager.loomWaveGenerator;
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		if (!OptionManager.loomWaveGenerator) {
			return 0;
		}
		
		if (!(world instanceof World)) {
			return 0;
		}
		
		return WaveGenerator.getLevelAt(((World) world).getRegistryKey(), pos);
	}
}
