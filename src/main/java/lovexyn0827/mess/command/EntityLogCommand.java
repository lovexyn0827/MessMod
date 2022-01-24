package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.EntityLogger;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class EntityLogCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entitylog")
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::subscribe)))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::unsubscribe)))
				.then(literal("listenField")
						.then(argument("field", StringArgumentType.word())
								.executes(EntityLogCommand::listenField)))
				.then(literal("flush")
						.executes((ct) -> {
							MessMod.INSTANCE.getEntityLogger().flushAll();
							CommandUtil.feedback(ct, "Flushed all changes");
							return Command.SINGLE_SUCCESS;
						}));
		dispatcher.register(command);
	}

	private static int subscribe(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		l.subscribe(list);
		CommandUtil.feedback(ct, "Subscribed " + list.size() + " Entities");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int unsubscribe(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		l.unsubscribe(list);
		CommandUtil.feedback(ct, "Unsubscribed " + list.size() + " Entities");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int listenField(CommandContext<ServerCommandSource> ct) {
		ct.getSource().sendFeedback(new LiteralText("Warning: Not Available Now")
				.formatted(Formatting.RED), false);
		ct.getSource().sendFeedback(new LiteralText("Warning: All active logs will be restarted")
				.formatted(Formatting.RED), false);
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		l.listenToField(StringArgumentType.getString(ct, "field"));
		return Command.SINGLE_SUCCESS;
	}
}
