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
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.EntityLogger;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntityLogCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggests = (ct,builder)->{
			EntityType<?> type = Registry.ENTITY_TYPE.get(EntitySummonArgumentType.getEntitySummon(ct, "entityType"));
			for(String fieldName : EntityFieldCommand.getAvailableFields(EntityLogger.ENTITY_TYPE_TO_CLASS.get(type))) {
				builder = builder.suggest(fieldName);
			}
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entitylog").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::subscribe)))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::unsubscribe)))
				.then(literal("listenField")
						.then(argument("entityType", EntitySummonArgumentType.entitySummon())
								.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
								.then(argument("field", StringArgumentType.word())
										.suggests(suggests)
										.executes(EntityLogCommand::listenField))))
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
		int i = l.subscribe(list);
		CommandUtil.feedback(ct, String.format("Found %d entities, and subscribed %d entities among them", 
				list.size(), i));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int unsubscribe(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		int i = l.unsubscribe(list);
		CommandUtil.feedback(ct, String.format("Found %d entities, and unsubscribed %d entities among them", 
				list.size(), i));
		return Command.SINGLE_SUCCESS;
	}
	
	private static int listenField(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		ct.getSource().sendFeedback(new LiteralText("Warning: Not Available Now")
				.formatted(Formatting.RED), false);
		ct.getSource().sendFeedback(new LiteralText("Warning: All active logs will be restarted")
				.formatted(Formatting.RED), false);
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Identifier id = EntitySummonArgumentType.getEntitySummon(ct, "entityType");
		try {
			l.listenToField(StringArgumentType.getString(ct, "field"), Registry.ENTITY_TYPE.get(id));
		} catch (Exception e) {
			CommandUtil.error(ct, e.getMessage());
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
