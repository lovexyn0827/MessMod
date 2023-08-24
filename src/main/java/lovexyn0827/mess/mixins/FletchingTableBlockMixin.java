package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FletchingTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.network.MessageType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(FletchingTableBlock.class)
public abstract class FletchingTableBlockMixin extends AbstractBlock {
	protected FletchingTableBlockMixin(Settings settings) {
		super(settings);
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if(OptionManager.fletchingTablePulseDetector && world instanceof ServerWorld) {
			detectPulse((ServerWorld) world, pos);
		}
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
					sw.getServer().getPlayerManager().broadcast(p.toText(), MessageType.SYSTEM, Util.NIL_UUID);
				});
	}
}
