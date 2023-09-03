package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.FormattedText;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(CraftingTableBlock.class)
public abstract class CraftingTableBlockMixin extends AbstractBlock {
	protected CraftingTableBlockMixin(Settings settings) {
		super(settings);
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if(OptionManager.craftingTableBUD && !world.isClient) {
			if(world instanceof ServerWorld) {
				ServerWorld sw = (ServerWorld) world;
				sw.getServer().getPlayerManager().broadcast(
						new FormattedText("NC: Tick %d @ (%d, %d, %d)", "cl", false, world.getTime(), 
								pos.getX(), pos.getY(), pos.getZ()).asMutableText(), false);
			}
		}
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
