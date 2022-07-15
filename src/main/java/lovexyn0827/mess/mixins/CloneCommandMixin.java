package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(CloneCommand.class)
// FIXME
public abstract class CloneCommandMixin {
	@Redirect(method = "execute", 
			at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/server/world/ServerWorld;"
					+ "isRegionLoaded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z")
	)
	private static boolean modifyToLoaded(ServerWorld world, BlockPos p1, BlockPos p2) {
		return world.isRegionLoaded(p1, p2) || OptionManager.disableChunkLoadingCheckInCommands;
	}
}
