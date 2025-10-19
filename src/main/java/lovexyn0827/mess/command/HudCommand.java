package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
import java.util.Collection;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.data.HudDataSender;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HudCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("hud").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("subField")
						.then(literal("target")
								.then(argument("entityType", StringArgumentType.word())
										.suggests(CommandUtil.ENTITY_TYPES)
										.then(argument("field", StringArgumentType.word())
												.suggests(CommandUtil.ENTITY_FIELDS_SUGGESTION)
												.executes((ct) -> {
													EntityType<?> type = Registry.ENTITY_TYPE
															.get(new Identifier(StringArgumentType.getString(ct, "entityType")));
													Class<?> cl = Reflection.ENTITY_TYPE_TO_CLASS.get(type);
													String field = StringArgumentType.getString(ct, "field");
													MessMod.INSTANCE.getServerHudManager().lookingHud
															.addField(cl, field);
													CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
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
																return Command.SINGLE_SUCCESS;
															} catch (Exception e) {
																e.printStackTrace();
																CommandUtil.errorRaw(ct, e.getMessage(), e);
																return 0;
															}
														})
														.then(argument("path", CommandUtil.getPathArgForFieldListening("entityType", "field"))
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
																		return Command.SINGLE_SUCCESS;
																	} catch (Exception e) {
																		e.printStackTrace();
																		CommandUtil.errorRaw(ct, e.getMessage(), e);
																		return 0;
																	}
																}))))))
						.then(literal("client").requires((s) -> !MessMod.isDedicatedEnv())
								.then(argument("field", StringArgumentType.word())
										.suggests((ct, builder) -> {
											Reflection.getAvailableFieldNames(ClientPlayerEntity.class).forEach(builder::suggest);
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
												.then(argument("path", AccessingPathArgumentType.accessingPathArg((ct) -> {
													String fName = StringArgumentType.getString(ct, "field");
													Field f = Reflection.getFieldFromNamed(
															MessMod.isDedicatedServerEnv() ? PlayerEntity.class : ClientPlayerEntity.class, fName);
													return f == null ? Object.class : f.getGenericType();
												}))
														.executes(ct -> {
															addListenedWithNameAndPath(ct, MessMod.INSTANCE.getServerHudManager().playerHudC, ClientPlayerEntity.class);
															return Command.SINGLE_SUCCESS;
														})))))
						.then(literal("server")
								.then(argument("field", StringArgumentType.word())
										.suggests((ct, builder) -> {
											Reflection.getAvailableFieldNames(ServerPlayerEntity.class).forEach(builder::suggest);
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
												.then(argument("path", AccessingPathArgumentType.accessingPathArg((ct) -> {
													String fName = StringArgumentType.getString(ct, "field");
													Field f = Reflection.getFieldFromNamed(ServerPlayerEntity.class, fName);
													return f == null ? Object.class : f.getGenericType();
												}))
														.executes(ct -> {
															addListenedWithNameAndPath(ct, MessMod.INSTANCE.getServerHudManager().playerHudS, ServerPlayerEntity.class);
															return Command.SINGLE_SUCCESS;
														}))))))
				.then(literal("unsubField")
						.then(literal("target")
								.then(argument("name", StringArgumentType.word())
										.executes((ct) -> {
											unsubscribe(MessMod.INSTANCE.getServerHudManager().lookingHud, ct);
											return Command.SINGLE_SUCCESS;
										})))
						.then(literal("client").requires((s) -> !MessMod.isDedicatedEnv())
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
				.then(literal("listFields")
						.then(literal("target")
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().lookingHud));
									return Command.SINGLE_SUCCESS;
								}))
						.then(literal("client").requires((s) -> !MessMod.isDedicatedEnv())
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().playerHudC));
									return Command.SINGLE_SUCCESS;
								}))
						.then(literal("server")
								.executes((ct) -> {
									CommandUtil.feedbackRaw(ct, listListenedFields(MessMod.INSTANCE.getServerHudManager().playerHudS));
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("setHudTarget")
						.then(argument("profile", GameProfileArgumentType.gameProfile())
								.executes((ct) -> {
									Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ct, "profile");
									if(profiles.size() != 1) {
										CommandUtil.error(ct, "cmd.hud.reqsinglepf");
										return 0;
									}
									
									MessMod.INSTANCE.getServerHudManager().setServerPlayerHudTarget(profiles.iterator().next());
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
	
	private static void unsubscribe(HudDataSender lookingHud, CommandContext<ServerCommandSource> ct) {
		String name = StringArgumentType.getString(ct, "name");
		if(lookingHud.removeCustomLine(name)) {
			CommandUtil.feedback(ct, "cmd.general.success");
		} else {

			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
		}
	}

	private static void addListenedWithNameAndPath(CommandContext<ServerCommandSource> ct, HudDataSender playerHudS, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		if(!playerHudS.addField(cl, field, name, path)) {
			CommandUtil.error(ct, "exp.dupfield");
		} else {
			CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field + '.' + path);
		}
	}
	
	private static void addListenedWithName(CommandContext<ServerCommandSource> ct, HudDataSender playerHudC, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		if(!playerHudC.addField(cl, field, name, null)) {
			CommandUtil.error(ct, "exp.dupfield");
		} else {
			CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
		}
	}
	
	private static void addListened(CommandContext<ServerCommandSource> ct, HudDataSender playerHudC, Class<?> cl) {
		String field = StringArgumentType.getString(ct, "field");
		if(!playerHudC.addField(cl, field)) {
			CommandUtil.error(ct, "exp.dupfield");
		} else {
			CommandUtil.feedbackWithArgs(ct, "cmd.entitylog.listen", field);
		}
	}

	private static String listListenedFields(HudDataSender lookingHud) {
		StringBuilder sb = new StringBuilder();
		lookingHud.getListenedFields().forEach((lf) -> {
			sb.append(lf.toString() + '\n');
		});
		
		return sb.toString();
	}
	
}
