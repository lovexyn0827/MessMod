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
import lovexyn0827.mess.log.entity.EntityLogger;
import lovexyn0827.mess.log.entity.SideLogStoragePolicy;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase;
import lovexyn0827.mess.util.phase.TickingPhaseArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class EntityLogCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entitylog").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::subscribe)
								.then(argument("policy", StringArgumentType.word())
										.suggests(CommandUtil.immutableSuggestionsOfEnum(SideLogStoragePolicy.class))
										.executes(EntityLogCommand::subscribeWithPolicy))))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes(EntityLogCommand::unsubscribe)))
				.then(literal("listenField")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.then(argument("field", StringArgumentType.word())
										.suggests(CommandUtil.ENTITY_FIELDS_SUGGESTION)
										.executes((ct) -> {
											EntityLogger l = MessMod.INSTANCE.getEntityLogger();
											EntityType<?> type = Registries.ENTITY_TYPE
													.get(Identifier.of(StringArgumentType.getString(ct, "entityType")));
											String field = StringArgumentType.getString(ct, "field");
											try {
												l.listenToField(field, type, null, null, ServerTickingPhase.TICKED_ALL_WORLDS);
												CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
											} catch (Exception e) {
												CommandUtil.errorRaw(ct, e.getMessage(), e);
											}
											
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("name", StringArgumentType.word())
												.executes((ct) -> {
													EntityLogger l = MessMod.INSTANCE.getEntityLogger();
													EntityType<?> type = Registries.ENTITY_TYPE
															.get(Identifier.of(StringArgumentType.getString(ct, "entityType")));
													String field = StringArgumentType.getString(ct, "field");
													String name = StringArgumentType.getString(ct, "name");
													try {
														l.listenToField(field, type, name, null, ServerTickingPhase.TICKED_ALL_WORLDS);
														CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
													} catch (Exception e) {
														CommandUtil.errorRaw(ct, e.getMessage(), e);
													}
													
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("path", CommandUtil.getPathArgForFieldListening("entityType", "field"))
														.executes((ct) -> {
															EntityLogger l = MessMod.INSTANCE.getEntityLogger();
															EntityType<?> type = Registries.ENTITY_TYPE
																	.get(Identifier.of(StringArgumentType.getString(ct, "entityType")));
															String field = StringArgumentType.getString(ct, "field");
															String name = StringArgumentType.getString(ct, "name");
															AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
															try {
																l.listenToField(field, type, name, path, ServerTickingPhase.TICKED_ALL_WORLDS);
																CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field + '.' + path);
															} catch (Exception e) {
																CommandUtil.errorRaw(ct, e.getMessage(), e);
															}
															
															return Command.SINGLE_SUCCESS;
														}))
												.then(TickingPhase.commandArg()
														.executes((ct) -> {
															EntityLogger l = MessMod.INSTANCE.getEntityLogger();
															EntityType<?> type = Registries.ENTITY_TYPE
																	.get(Identifier.of(StringArgumentType.getString(ct, "entityType")));
															String field = StringArgumentType.getString(ct, "field");
															String name = StringArgumentType.getString(ct, "name");
															TickingPhase phase = TickingPhaseArgumentType.getPhase(ct, "whereToUpdate");
															try {
																l.listenToField(field, type, name, null, phase);
																CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
															} catch (Exception e) {
																CommandUtil.errorRaw(ct, e.getMessage(), e);
															}
															
															return Command.SINGLE_SUCCESS;
														})
														.then(argument("path", CommandUtil.getPathArgForFieldListening("entityType", "field"))
																.executes((ct) -> {
																	EntityLogger l = MessMod.INSTANCE.getEntityLogger();
																	EntityType<?> type = Registries.ENTITY_TYPE
																			.get(Identifier.of(StringArgumentType.getString(ct, "entityType")));
																	String field = StringArgumentType.getString(ct, "field");
																	String name = StringArgumentType.getString(ct, "name");
																	AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
																	TickingPhase phase = TickingPhaseArgumentType.getPhase(ct, "whereToUpdate");
																	try {
																		l.listenToField(field, type, name, path, phase);
																		CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field + '.' + path);
																	} catch (Exception e) {
																		CommandUtil.errorRaw(ct, e.getMessage(), e);
																	}
																	
																	return Command.SINGLE_SUCCESS;
																})))))))
				.then(literal("stopListenField")
						.then(argument("field", StringArgumentType.word())
								.suggests((ct, b) -> {
									MessMod.INSTANCE.getEntityLogger().getListenedFields().values().forEach((f) -> {
										b.suggest(f.getName());
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
							CommandUtil.feedback(ct, "cmd.general.flush");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("autoSub")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									Identifier id = Identifier.of(StringArgumentType.getString(ct, "entityType"));
									EntityType<?> type = Registries.ENTITY_TYPE.get(id);
									MessMod.INSTANCE.getEntityLogger().addAutoSubEntityType(type);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.autosub", id);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("stopAutoSub")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									Identifier id = Identifier.of(StringArgumentType.getString(ct, "entityType"));
									EntityType<?> type = Registries.ENTITY_TYPE.get(id);
									MessMod.INSTANCE.getEntityLogger().removeAutoSubEntityType(type);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.stopautosub", id);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("autoSubName")
						.then(argument("name", StringArgumentType.word())
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									MessMod.INSTANCE.getEntityLogger().addAutoSubName(name);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.autosubname", name);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("stopAutoSubName")
						.then(argument("name", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									MessMod.INSTANCE.getEntityLogger().removeAutoSubName(name);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.stopautosubname", name);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("countLoggedEntities")
						.executes((ct) -> {
							CommandUtil.feedbackWithArgs(ct, "cmd.general.count", 
									MessMod.INSTANCE.getEntityLogger().countLoggedEntities());
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("setDefaultStoragePolicy")
						.then(argument("policy", StringArgumentType.word())
								.suggests(CommandUtil.immutableSuggestionsOfEnum(SideLogStoragePolicy.class))
								.executes((ct) -> {
									SideLogStoragePolicy policy;
									String name = StringArgumentType.getString(ct, "policy");
									try {
										policy = SideLogStoragePolicy.valueOf(name);
									} catch (IllegalArgumentException e) {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
										return 0;
									}
									
									MessMod.INSTANCE.getEntityLogger().setDefaultStoragePolicy(policy);
									CommandUtil.feedback(ct, "cmd.general.success");
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
	
	private static int subscribeWithPolicy(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		SideLogStoragePolicy policy;
		String name = StringArgumentType.getString(ct, "policy");
		try {
			policy = SideLogStoragePolicy.valueOf(name);
		} catch (IllegalArgumentException e) {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
			return 0;
		}
		
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		Collection<? extends Entity> list = EntityArgumentType.getEntities(ct, "target");
		int i = l.subscribe(list, policy);
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

	private static int unlistenField(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		EntityLogger l = MessMod.INSTANCE.getEntityLogger();
		String name = StringArgumentType.getString(ct, "field");
		try {
			l.unlistenToField(name);
			CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.unlisten", name);
			return Command.SINGLE_SUCCESS;
		} catch (Exception e) {
			CommandUtil.error(ct, e.getMessage());
			return 0;
		}
	}
}
