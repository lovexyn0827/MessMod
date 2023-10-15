package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.access.InvalidLiteralException;
import lovexyn0827.mess.util.access.Literal;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class EntityFieldCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggests = (ct,builder)->{
			for(String fieldName : Reflection.getAvailableFieldNames(EntityArgumentType.getEntity(ct, "target").getClass())) {
				builder = builder.suggest(fieldName);
			}
			
			builder.suggest("-THIS-");
			return builder.buildFuture();
		};
		
		LiteralArgumentBuilder<ServerCommandSource> get = literal("get")
				.then(argument("fieldName",StringArgumentType.string())
						.suggests(suggests)
						.executes((ct) -> getField(ct, AccessingPath.DUMMY))
						.then(argument("path", AccessingPathArgumentType.accessingPathArg())
								.executes((ct) -> {
									try {
										AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
										return getField(ct, path);
									} catch (TranslatableException e) {
										ct.getSource().sendError(new LiteralText(e.getMessage()));
										return 0;
									}
								})));
		
		ArgumentBuilder<ServerCommandSource, ?> modify = literal("modify")
				.requires((source) -> source.hasPermissionLevel(1))
				.then(argument("fieldName",StringArgumentType.string()).suggests(suggests)
						.then(argument("newValue",StringArgumentType.string())
								.executes((ct)->{
									try {
										if(modifyField(EntityArgumentType.getEntity(ct, "target"),
												StringArgumentType.getString(ct, "fieldName"),
												StringArgumentType.getString(ct, "newValue"), 
												null, 
												ct)) {
											CommandUtil.feedback(ct, "cmd.entityfield.modify.success");
											return Command.SINGLE_SUCCESS;
										}
										
										return 0;
									} catch (Exception e) {
										CommandUtil.error(ct, "cmd.entityfield.modify.failure", e);
										return -1;
									}
								})
								.then(argument("path", AccessingPathArgumentType.accessingPathArg())
										.executes((ct)->{
											try {
												if(modifyField(EntityArgumentType.getEntity(ct, "target"),
														StringArgumentType.getString(ct, "fieldName"),
														StringArgumentType.getString(ct, "newValue"), 
														AccessingPathArgumentType.getAccessingPath(ct, "path"), 
														ct)) {
													CommandUtil.feedback(ct, "cmd.entityfield.modify.success");
													return Command.SINGLE_SUCCESS;
												}
												
												return 0;
											} catch (TranslatableException e) {
												ct.getSource().sendError(new LiteralText(e.getMessage()));
												return 0;
											} catch (Exception e) {
												CommandUtil.error(ct, "cmd.entityfield.modify.failure", e);
												return 0;
											}
										}))));
		
		ArgumentBuilder<ServerCommandSource, ?> listAll = literal("listAvailableFields")
				.executes((ct) -> {
					Set<String> fieldSet;
					try {
						fieldSet = Reflection.getAvailableFieldNames(EntityArgumentType.getEntity(ct, "target").getClass());
						String list = String.join("  ", fieldSet);
						CommandUtil.feedback(ct, list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					return Command.SINGLE_SUCCESS;
				});
		
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entityfield")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("target",EntityArgumentType.entity())
						.then(get).then(modify).then(listAll));
		dispatcher.register(command);
	}
	
	private static int getField(CommandContext<ServerCommandSource> ct, AccessingPath path) throws CommandSyntaxException {
		Entity entity = EntityArgumentType.getEntity(ct, "target");
		String name = StringArgumentType.getString(ct, "fieldName");
		if("-THIS-".equals(name)) {
			try {
				CommandUtil.feedbackRaw(ct, path.access(entity, entity.getClass()));
				return Command.SINGLE_SUCCESS;
			} catch (AccessingFailureException e) {
				CommandUtil.errorRaw(ct, e.getMessage(), e);
				return 0;
			}
		}
		
		try {
			Field field = Reflection.getFieldFromNamed(entity.getClass(), name);
			if(field != null) {
				field.setAccessible(true);
				Object ob = field.get(entity);
				CommandUtil.feedbackRaw(ct,  ob == null ? "[null]" : path.access(ob, field.getGenericType()));
			} else {
				CommandUtil.error(ct, "cmd.entityfield.nosuchfield");
			}
			
			return Command.SINGLE_SUCCESS;
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
			CommandUtil.error(ct, "cmd.entityfield.unexpected", e);
			return 0;
		} catch (NoSuchFieldError e) {
			CommandUtil.error(ct, "cmd.entityfield.nosuchfield");
			return 0;
		} catch (TranslatableException e) {
			CommandUtil.errorRaw(ct, e.getMessage(), e);
			e.printStackTrace();
			return 0;
		} catch (AccessingFailureException e) {
			CommandUtil.errorRaw(ct, e.getMessage(), e);
			return 0;
		}
	}

	private static boolean modifyField(Entity entity, String fieldName, String newValue, AccessingPath path, 
			CommandContext<ServerCommandSource> ct) 
			throws IllegalAccessException, CommandSyntaxException, AccessingFailureException {
		Object obj;
		Type type;
		if("-THIS-".equals(fieldName)) {
			obj = entity;
			type = entity.getClass();
		} else {
			Field field = Reflection.getFieldFromNamed(entity.getClass(), fieldName);
			if(field == null) {
				throw new IllegalArgumentException("cmd.entityfield.nosuchfield");
			}

			field.setAccessible(true);
			if(path == null) {
				try {
					field.set(entity, Literal.parse(newValue).get(field.getGenericType()));
				} catch (InvalidLiteralException e) {
					String msg = e.getMessage();
					CommandUtil.error(ct, msg == null ? "~null~" : msg, e);
					return false;
				} catch (IllegalArgumentException e) {
					CommandUtil.errorWithArgs(ct, "cmd.entityfield.modify.dismatch", 
							field.getType().getCanonicalName());
					return false;
				}
				
				return true;
			} else {
				obj = field.get(entity);
				type = field.getGenericType();
			}
			
		}
		
		try {
			path.write(obj, type, newValue);
			return true;
		} catch (AccessingFailureException e) {
			CommandUtil.errorRaw(ct, e.getMessage(), e);
			return false;
		}
	}
}
