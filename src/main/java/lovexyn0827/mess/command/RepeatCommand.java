package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.ServerCommandSource;

public class RepeatCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("repeat")
				.then(argument("times", IntegerArgumentType.integer(0))
						.then(argument("feedbacks", BoolArgumentType.bool())
								.fork(dispatcher.getRoot(), (ct) -> {
									return Collections.nCopies(IntegerArgumentType.getInteger(ct, "times"), 
											BoolArgumentType.getBool(ct, "feedbacks") ? ct.getSource() : CommandUtil.noreplySourceFor(ct.getSource()));
								})));
		dispatcher.register(command);
	}
}
