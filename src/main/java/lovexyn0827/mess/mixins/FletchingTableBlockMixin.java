package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

@Mixin(FletchingTableBlock.class)
public abstract class FletchingTableBlockMixin extends Block {
	protected FletchingTableBlockMixin(Settings settings) {
		super(settings);
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state,
			WorldView world,
			ScheduledTickView tickView,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			Random random) {
		if(OptionManager.fletchingTablePulseDetector && world instanceof ServerWorld) {
			detectPulse((ServerWorld) world, pos);
		}
		
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, 
				neighborPos, neighborState, random);
	}
	
	@Override
	public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
		if(OptionManager.fletchingTablePulseDetector && world instanceof ServerWorld) {
			detectPulse((ServerWorld) world, pos);
		}
	}
	
	private static void detectPulse(ServerWorld sw, BlockPos pos) {
		boolean powered = sw.isReceivingRedstonePower(pos);
		((ServerWorldInterface) sw)
				.getPulseRecorder()
				.setSignalLevel(pos, powered)
				.ifPresent((p) -> {
					sw.getServer().getPlayerManager().broadcast(p.toText(), false);
				});
	}
}
