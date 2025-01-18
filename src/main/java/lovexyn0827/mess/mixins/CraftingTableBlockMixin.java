package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.FormattedText;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

@Mixin(CraftingTableBlock.class)
public abstract class CraftingTableBlockMixin extends Block {
	protected CraftingTableBlockMixin(Settings settings) {
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
		if(OptionManager.craftingTableBUD && !world.isClient()) {
			if(world instanceof ServerWorld) {
				ServerWorld sw = (ServerWorld) world;
				sw.getServer().getPlayerManager().broadcast(
						new FormattedText("NC: Tick %d @ (%d, %d, %d)", "cl", false, sw.getTime(), 
								pos.getX(), pos.getY(), pos.getZ()).asMutableText(), false);
			}
		}
		
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, 
				neighborPos, neighborState, random);
	}
	
	@Override
	public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
		if(OptionManager.craftingTableBUD && world instanceof ServerWorld) {
			ServerWorld sw = (ServerWorld) world;
			sw.getServer().getPlayerManager().broadcast(
					new FormattedText("PP: Tick %d @ (%d, %d, %d)", "1l", false, sw.getTime(), 
							pos.getX(), pos.getY(), pos.getZ()).asMutableText(), false);
		}
	}
}
