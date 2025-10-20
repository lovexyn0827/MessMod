package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.electronic.OscilscopeScreen;
import lovexyn0827.mess.options.OptionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HayBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(HayBlock.class)
public abstract class HayBlockMixin extends AbstractBlock {
	public HayBlockMixin(Settings settings) {
		super(settings);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (OptionManager.hayOscilloscope && world.isClient) {
			MinecraftClient.getInstance().setScreen(new OscilscopeScreen());
		}
		
		return ActionResult.CONSUME;
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if(OptionManager.hayOscilloscope && world instanceof ServerWorld) {
			onUpdate((ServerWorld) world, pos);
		}
	}
	

	@Override
	public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
		if(OptionManager.hayOscilloscope && world instanceof ServerWorld) {
			onUpdate((ServerWorld) world, pos);
		}
	}
	
	protected void onUpdate(ServerWorld world, BlockPos pos) {
		MessMod.INSTANCE.getOscilscope().update(world, pos);
	}
}
