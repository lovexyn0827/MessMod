package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class LogMovementCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logmovement").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									long count = l.stream().filter((e) -> !((EntityInterface) e).shouldLogMovement())
											.map((e) -> {
												((EntityInterface) e).setMovementSubscribed(true);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.general.sub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									long count = l.stream().filter((e) -> ((EntityInterface) e).shouldLogMovement())
											.map((e) -> {
												((EntityInterface) e).setMovementSubscribed(false);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.general.unsub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
