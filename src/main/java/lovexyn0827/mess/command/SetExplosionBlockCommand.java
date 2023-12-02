package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class SetExplosionBlockCommand {
	private static BlockState blockState = null;
	private static BlockState fireState = null;
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("setexplodeblock").requires(CommandUtil.COMMAND_REQUMENT).
				then(argument("blockState",BlockStateArgumentType.blockState()).
						then(argument("fireState",BlockStateArgumentType.blockState()).
								executes((ct->{
									blockState = BlockStateArgumentType.getBlockState(ct, "blockState").getBlockState();
									fireState = BlockStateArgumentType.getBlockState(ct, "fireState").getBlockState();
									CommandUtil.feedback(ct, "cmd.general.success");
									return 1;
								}))));
		dispatcher.register(command);
	}
	
	public static void reset() {
		blockState = null;
		fireState = null;
	}
	
	public static BlockState getBlockState() {
		return blockState;
	}
	
	public static BlockState getFireState() {
		return fireState;
	}
}
