package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnsureCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("ensure").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("pos", BlockPosArgumentType.blockPos())
						.executes((ct) -> {
								BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
								World world = ct.getSource().getWorld();
								BlockState state = world.getBlockState(pos);
								BlockEntity be = world.getBlockEntity(pos);
								String posStr = "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
								CommandUtil.feedbackWithArgs(ct, "cmd.ensure.blockat", posStr, state);
								if(be != null) {
									CommandUtil.feedbackWithArgs(ct, "cmd.ensure.be", BlockEntityType.getId(be.getType()));
								}
								
								return Command.SINGLE_SUCCESS;
						}));
		dispatcher.register(command);
	}
}
