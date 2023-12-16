package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Shadow
	public ServerPlayerEntity player;
	@Inject(
			method = "tryBreakBlock", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/server/world/ServerWorld.removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void recordBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> ci, 
			BlockEntity blockEntity, Block block, BlockState blockState) {
		if(OptionManager.blockPlacementHistory) {
			BlockPlacementHistory history = ((ServerPlayerEntityInterface) this.player).getBlockPlacementHistory();
			if(history != null) {
				history.pushSingle(pos, blockState, Blocks.AIR.getDefaultState(), blockEntity);
			}
		}
	}
	
	@Inject(method = "interactItem", at = @At("HEAD"))
	public void onPlayerUseItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, 
			CallbackInfoReturnable<ActionResult> cir) {
		if(OptionManager.enabledTools) {
			boolean carpetLoaded = FabricLoader.getInstance().isModLoaded("carpet");
			ServerCommandSource source = CommandUtil.noreplySourceFor(this.player.getCommandSource());
			if(stack.getItem() == Items.BRICK && carpetLoaded) {
				this.player.server.getCommandManager().executeWithPrefix(source, "/tick freeze");
			} else if(stack.getItem() == Items.BONE && carpetLoaded) {
				this.player.server.getCommandManager().executeWithPrefix(source, 
						String.format("/tick step %d", stack.getCount()));
			} else if(stack.getItem() == Items.NETHERITE_INGOT) {
				for(ServerWorld serverWorld : player.getServer().getWorlds()) {
					for(Entity e : serverWorld.getEntitiesByType(TypeFilter.instanceOf(Entity.class), (e) -> !(e instanceof ServerPlayerEntity))) {
						e.remove(Entity.RemovalReason.KILLED);
					}
				}
			}
		}
	}
}
