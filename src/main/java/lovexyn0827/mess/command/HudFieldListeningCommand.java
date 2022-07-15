package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.data.HudDataSenderer;
import lovexyn0827.mess.rendering.hud.data.PlayerHudDataSenderer;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HudFieldListeningCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("hudfieldlistening").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(literal("target")
								.then(argument("entityType", StringArgumentType.word())
										.suggests(CommandUtil.ENTITY_TYPES)
										.then(argument("field", StringArgumentType.word())
												.suggests(CommandUtil.FIELDS_SUGGESTION)
												.executes((ct) -> {
													EntityType<?> type = Registry.ENTITY_TYPE
															.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
													Class<?> cl = Reflection.ENTITY_TYPE_TO_CLASS.get(type);
													MessMod.INSTANCE.getServerHudManager().lookingHud
															.addField(cl, StringArgumentType.getString(ct, "field"));
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("name", StringArgumentType.word())
														.executes((ct) -> {
															EntityType<?> type = Registry.ENTITY_TYPE
																	.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
															Class<?> cl = Reflection.ENTITY_TYPE_TO_CLASS.get(type);
															String field = StringArgumentType.getString(ct, "field");
															String name = StringArgumentType.getString(ct, "name");
															try {
																MessMod.INSTANCE.getServerHudManager().lookingHud.addField(cl, field, name, null);
																CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
															} catch (Exception e) {
																e.printStackTrace();
																CommandUtil.errorRaw(ct, e.getMessage(), e);
															}
															
															return Command.SINGLE_SUCCESS;
														})
														.then(argument("path", AccessingPathArgumentType.accessingPathArg())
																.executes((ct) -> {
																	EntityType<?> type = Registry.ENTITY_TYPE
																			.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
																	Class<?> cl = Reflection.ENTITY_TYPE_TO_CLASS.get(type);
																	String field = StringArgumentType.getString(ct, "field");
																	String name = StringArgumentType.getString(ct, "name");
																	AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
																	try {
																		MessMod.INSTANCE.getServerHudManager().lookingHud.addField(cl, field, name, path);
																		CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field + '.' + path);
																	} catch (Exception e) {
																		e.printStackTrace();
																		CommandUtil.errorRaw(ct, e.getMessage(), e);
																	}
																	
																	return Command.SINGLE_SUCCESS;
																}))))))
						.then(literal("client")
								.then(argument("field", StringArgumentType.word())
										.suggests((ct, builder) -> {
											Reflection.getAvailableFields(ClientPlayerEntity.class).forEach(builder::suggest);
											return builder.buildFuture();
										})
										.executes((ct) -> {
											addListened(ct, MessMod.INSTANCE.getServerHudManager().playerHudC, ClientPlayerEntity.class);
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("name", StringArgumentType.word())
												.executes((ct) -> {
													addListenedWithName(ct, MessMod.INSTANCE.getServerHudManager().playerHudC, ClientPlayerEntity.class);
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("path", AccessingPathArgumentType.accessingPathArg())
														.executes(ct -> {
															addListenedWithNameAndPath(ct, MessMod.INSTANCE.getServerHudManager().playerHudC, ClientPlayerEntity.class);
															return Command.SINGLE_SUCCESS;
														})))))
						.then(literal("server")
								.then(argument("field", StringArgumentType.word())
										.suggests((ct, builder) -> {
											Reflection.getAvailableFields(ServerPlayerEntity.class).forEach(builder::suggest);
											return builder.buildFuture();
										})
										.executes((ct) -> {
											addListened(ct, MessMod.INSTANCE.getServerHudManager().playerHudS, ServerPlayerEntity.class);
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("name", StringArgumentType.word())
												.executes((ct) -> {
													addListenedWithName(ct, MessMod.INSTANCE.getServerHudManager().playerHudS, ServerPlayerEntity.class);
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("path", AccessingPathArgumentType.accessingPathArg())
														.executes(ct -> {
															addListenedWithNameAndPath(ct, MessMod.INSTANCE.getServerHudManager().playerHudS, ServerPlayerEntity.class);
															return Command.SINGLE_SUCCESS;
														}))))))
				.then(literal("unsub")
						.then(literal("target")
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											unsubscribe(MessMod.INSTANCE.getServerHudManager().lookingHud, ct);
											return Command.SINGLE_SUCCESS;
										})))
						.then(literal("client")
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											unsubscribe(MessMod.INSTANCE.getServerHudManager().playerHudC, ct);
											return Command.SINGLE_SUCCESS;
										})))
						.then(literal("server")
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											unsubscribe(MessMod.INSTANCE.getServerHudManager().playerHudS, ct);
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("list")
						.then(literal("target")
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().lookingHud));
									return Command.SINGLE_SUCCESS;
								}))
						.then(literal("client")
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().playerHudC));
									return Command.SINGLE_SUCCESS;
								}))
						.then(literal("server")
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().playerHudS));
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
	
	private static void unsubscribe(HudDataSenderer lookingHud, CommandContext<ServerCommandSource> ct) {
		String name = StringArgumentType.getString(ct, "name");
		lookingHud.removeField(name);
	}

	private static void addListenedWithNameAndPath(CommandContext<ServerCommandSource> ct, PlayerHudDataSenderer playerHudS, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		if(!playerHudS.addField(cl, field, name, path)) {
			CommandUtil.error(ct, "exp.dupfield");
		}
	}
	
	private static void addListenedWithName(CommandContext<ServerCommandSource> ct, PlayerHudDataSenderer playerHudC, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		if(!playerHudC.addField(cl, field, name, null)) {
			CommandUtil.error(ct, "exp.dupfield");
		}
	}
	
	private static void addListened(CommandContext<ServerCommandSource> ct, PlayerHudDataSenderer playerHudC, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		if(!playerHudC.addField(cl, field)) {
			CommandUtil.error(ct, "exp.dupfield");
		}
	}

	private static String listListenedFields(HudDataSenderer lookingHud) {
		StringBuilder sb = new StringBuilder();
		lookingHud.getListenedFields().forEach((lf) -> {
			sb.append(lf.toString() + '\n');
		});
		
		return sb.toString();
	}
	
}
