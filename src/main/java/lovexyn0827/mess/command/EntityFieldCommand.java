package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;

public class EntityFieldCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggests = (ct,builder)->{
			for(String fieldName : Reflection.getAvailableFields(EntityArgumentType.getEntity(ct, "target").getClass())) {
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
										e.printStackTrace();
										ct.getSource().sendError(new LiteralText(e.getMessage()));
										return 0;
									}
								})));
		
		ArgumentBuilder<ServerCommandSource, ?> modify = literal("modify")
				.requires((source)->source.hasPermissionLevel(1))
				.then(argument("fieldName",StringArgumentType.string()).suggests(suggests)
						.then(argument("newValue",StringArgumentType.string())
								.executes((ct)->{
									try {
										modifyField(EntityArgumentType.getEntity(ct, "target"),
												StringArgumentType.getString(ct, "fieldName"),
												StringArgumentType.getString(ct, "newValue"));
										CommandUtil.feedback(ct, "cmd.entityfield.modify.success");
										return 1;
									} catch (Exception e) {
										CommandUtil.error(ct, "cmd.entityfield.modify.failure", e);
										return -1;
									}
								})));
		
		ArgumentBuilder<ServerCommandSource, ?> listAll = literal("listAvailableFields")
				.executes((ct)->{
					Set<String> fieldSet;
					try {
						fieldSet = Reflection.getAvailableFields(EntityArgumentType.getEntity(ct, "target").getClass());
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

	private static boolean modifyField(Entity entity, String fieldName, String newValue) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Field field = Reflection.getFieldFromNamed(entity.getClass(), fieldName);
		if(field == null) {
			throw new IllegalArgumentException("cmd.entityfield.nosuchfield");
		}
		
		Class<?> type = field.getType();
		if(type == Integer.TYPE) {
			field.set(entity, Integer.parseInt(newValue));
		} else if (type == Float.TYPE) {
			field.set(entity, Float.parseFloat(newValue));
		} else if (type == Double.TYPE) {
			field.set(entity, Double.parseDouble(newValue));
		} else if (type == String.class) {
			field.set(entity, newValue);
		} else if (type == Vec3d.class) {
			String[] subVals = newValue.split(",");
			if(subVals.length != 3) throw new IllegalArgumentException("cmd.entityfield.modify.vectorsyntax");
			Vec3d vec3d = new Vec3d(Double.parseDouble(subVals[0]),
					Double.parseDouble(subVals[1]),
					Double.parseDouble(subVals[2]));
			field.set(entity, vec3d);
		} else if (type == Boolean.TYPE){
			if((!newValue.equals("true")) && (!newValue.equals("false"))) throw new IllegalArgumentException("Use true or false");
			field.set(entity, Boolean.parseBoolean(newValue));
		} else {
			throw new IllegalArgumentException("cmd.entityfield.modify.unsupported");
		}
		
		return true;
	}
}
