package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockPosArgumentType.class)
public class BlockPosArgumentTypeMixin {
	@Inject(method = "getLoadedBlockPos", 
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private static void returnIfNeeded(CommandContext<ServerCommandSource> context, String name, CallbackInfoReturnable<BlockPos> cir) {
		if(OptionManager.disableChunkLoadingCheckInCommands) {
			BlockPos blockPos = ((PosArgument)context.getArgument(name, PosArgument.class))
					.toAbsoluteBlockPos((ServerCommandSource)context.getSource());
			cir.setReturnValue(blockPos);
			cir.cancel();
		}
	}
}
