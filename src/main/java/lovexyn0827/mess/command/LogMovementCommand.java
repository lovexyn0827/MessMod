package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class LogMovementCommand {
	public static final Set<Entity> SUBSCRIBED_ENTITIES = Sets.newHashSet();
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logmovement").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									SUBSCRIBED_ENTITIES.addAll(l);
									CommandUtil.feedback(ct, "Subscribed " + l.size() + " Entities");
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									SUBSCRIBED_ENTITIES.removeAll(l);
									CommandUtil.feedback(ct, "Unsubscribed " + l.size() + " Entities");
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
