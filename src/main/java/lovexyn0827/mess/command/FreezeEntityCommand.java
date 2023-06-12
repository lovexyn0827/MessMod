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

public class FreezeEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("freezentity").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("freeze")
						.then(argument("entities", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "entities");
									long count = l.stream().filter((e) -> !((EntityInterface) e).isFrozen())
											.map((e) -> {
												((EntityInterface) e).setFrozen(true);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.freezentity.sub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("resume")
						.then(argument("entities", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "entities");
									long count = l.stream().filter((e) -> ((EntityInterface) e).isFrozen())
											.map((e) -> {
												((EntityInterface) e).setFrozen(false);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.freezentity.unsub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})));;
		dispatcher.register(command);
	}
}
