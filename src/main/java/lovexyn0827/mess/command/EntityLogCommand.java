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
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EntityLogCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entitylog").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::subscribe)))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::unsubscribe)))
				.then(literal("listenField")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.then(argument("field", StringArgumentType.word())
										.suggests(CommandUtil.FIELDS_SUGGESTION)
										.executes((ct) -> {
											sendRestartWarnings(ct);
											EntityLogger l = MessMod.INSTANCE.getEntityLogger();
											EntityType<?> type = Registry.ENTITY_TYPE
													.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
											String field = StringArgumentType.getString(ct, "field");
											try {
												l.listenToField(field, type, null, null);
												CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
											} catch (Exception e) {
												CommandUtil.errorRaw(ct, e.getMessage(), e);
											}
											
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("name", StringArgumentType.word())
												.executes((ct) -> {
													sendRestartWarnings(ct);
													EntityLogger l = MessMod.INSTANCE.getEntityLogger();
													EntityType<?> type = Registry.ENTITY_TYPE
															.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
													String field = StringArgumentType.getString(ct, "field");
													String name = StringArgumentType.getString(ct, "name");
													try {
														l.listenToField(field, type, name, null);
														CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
													} catch (Exception e) {
														CommandUtil.errorRaw(ct, e.getMessage(), e);
													}
													
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("path", AccessingPathArgumentType.accessingPathArg())
														.executes((ct) -> {
															sendRestartWarnings(ct);
															EntityLogger l = MessMod.INSTANCE.getEntityLogger();
															EntityType<?> type = Registry.ENTITY_TYPE
																	.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
															String field = StringArgumentType.getString(ct, "field");
															String name = StringArgumentType.getString(ct, "name");
															AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
															try {
																l.listenToField(field, type, name, path);
																CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field + '.' + path);
															} catch (Exception e) {
																CommandUtil.errorRaw(ct, e.getMessage(), e);
															}
															
															return Command.SINGLE_SUCCESS;
														}))))))
				.then(literal("stopListenField")
						.then(argument("field", StringArgumentType.word())
								.suggests((ct, b) -> {
									MessMod.INSTANCE.getEntityLogger().getListenedFields().values().forEach((f) -> {
										b.suggest(f.getCustomName());
									});
									return b.buildFuture();
								})
								.executes(EntityLogCommand::unlistenField)))
				.then(literal("listListenedFields")
						.executes((ct) -> {
							StringBuilder sb = new StringBuilder();
							MessMod.INSTANCE.getEntityLogger().getListenedFields().values().forEach((f) -> {
								sb.append("\n");
								sb.append(f.toString());
							});
							CommandUtil.feedbackRaw(ct, sb.toString());
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("flush")
						.executes((ct) -> {
							MessMod.INSTANCE.getEntityLogger().flushAll();
							CommandUtil.feedback(ct, "Flushed all changes");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("autoSub")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									EntityType<?> type = Registry.ENTITY_TYPE
											.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
									MessMod.INSTANCE.getEntityLogger().addAutoSubEntityType(type);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("stopAutoSub")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									EntityType<?> type = Registry.ENTITY_TYPE
											.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
									MessMod.INSTANCE.getEntityLogger().removeAutoSubEntityType(type);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}

	private static int subscribe(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		int i = l.subscribe(list);
		CommandUtil.feedbackWithArgs(ct, "cmd.general.sub", list.size(), i);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int unsubscribe(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		int i = l.unsubscribe(list);
		CommandUtil.feedbackWithArgs(ct, "cmd.general.unsub", list.size(), i);
		return Command.SINGLE_SUCCESS;
	}
	
	private static void sendRestartWarnings(CommandContext<ServerCommandSource> ct) {
		ct.getSource().sendFeedback(new LiteralText(I18N.translate("cmd.entitylog.restart"))
				.formatted(Formatting.RED), false);
	}

	private static int unlistenField(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		sendRestartWarnings(ct);
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		String name = StringArgumentType.getString(ct, "field");
		try {
			l.unlistenToField(name);
			CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.unlisten", name);
		} catch (Exception e) {
			CommandUtil.error(ct, e.getMessage());
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
