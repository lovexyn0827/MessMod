package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;

public class EntityFieldCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggests = (ct,builder)->{
			for(String fieldName:getAvailableFields(EntityArgumentType.getEntity(ct, "target"))) {
				builder = builder.suggest(fieldName);
			}
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entityfield").
				then(argument("target",EntityArgumentType.entity()).
						then(literal("get").
								then(argument("fieldName",StringArgumentType.string()).suggests(suggests).
										executes((ct)->{
											try {
												Entity entity = EntityArgumentType.getEntity(ct, "target");
												Object ob = getField(entity.getClass(),StringArgumentType.getString(ct, "fieldName")).get(entity);
												ct.getSource().sendFeedback(new LiteralText(ob.toString()), false);
											} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
												ct.getSource().sendError(new LiteralText("Unexpected exception in getting the field:"+e));
												return -1;
											}
											return 0;
										}))).
						then(literal("modify").requires((source)->source.hasPermissionLevel(1)).
								then(argument("fieldName",StringArgumentType.string()).suggests(suggests).
										then(argument("newValue",StringArgumentType.string()).
												executes((ct)->{
													try {
														modifyField(EntityArgumentType.getEntity(ct, "target"),
																StringArgumentType.getString(ct, "fieldName"),
																StringArgumentType.getString(ct, "newValue"));
														ct.getSource().sendFeedback(new LiteralText("Field modified successfully"), false);
														return 1;
													} catch (Exception e) {
														ct.getSource().sendError(new LiteralText("Failed to modify thhe field:"+e));
														return -1;
													}
												})))).
						then(literal("listAvailableFields").
								executes((ct)->{
									Set<String> fieldSet;
									try {
										fieldSet = getAvailableFields(EntityArgumentType.getEntity(ct, "target"));
										LiteralText list = new LiteralText(String.join("\t", fieldSet));
										ct.getSource().sendFeedback(list, false);
									} catch (Exception e) {
										e.printStackTrace();
									}
									return 0;
								})));
		dispatcher.register(command);
	}

	private static boolean modifyField(Entity entity, String fieldName, String newValue) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Field field = getField(entity.getClass(), fieldName);
		if(field==null) {
			throw new IllegalArgumentException("Field not found in class"+entity.getClass());
		}
		Class<?> type = field.getType();
		if(type==Integer.TYPE) {
			field.set(entity, Integer.parseInt(newValue));
		}else if(type==Float.TYPE) {
			field.set(entity, Float.parseFloat(newValue));
		}else if(type==Double.TYPE) {
			field.set(entity, Double.parseDouble(newValue));
		}else if(type==String.class) {
			field.set(entity, newValue);
		}else if(type==Vec3d.class) {
			String[] subVals = newValue.split(",");
			if(subVals.length!=3) throw new IllegalArgumentException("Too many or too few nembers given!");
			Vec3d vec3d = new Vec3d(Double.parseDouble(subVals[0]),
					Double.parseDouble(subVals[1]),
					Double.parseDouble(subVals[2]));
			field.set(entity, vec3d);
		}else if(type==Boolean.TYPE){
			if((!newValue.equals("true"))&&(!newValue.equals("false"))) throw new IllegalArgumentException("Use true or false");
			field.set(entity, Boolean.parseBoolean(newValue));
		}else{
			throw new IllegalArgumentException("Unsupported field given!");
		}
		return true;
	}

	private static Set<String> getAvailableFields(Entity entity) {
		Class<?> entityClass = entity.getClass();
		Set<String> fieldSet = new TreeSet<>();
		while(entityClass!=Object.class) {
			for(Field field:entityClass.getDeclaredFields()) {
				fieldSet.add(field.getName());
			}
			entityClass = entityClass.getSuperclass();
		}
		return fieldSet;
	}

	private static Field getField(Class<?> targetClass, String fieldName) {
		while(true) {
			try {
				Field field = targetClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			}catch(NoSuchFieldException e) {
				if(!Object.class.equals(targetClass)) {
					targetClass = targetClass .getSuperclass();
					continue;
				}
				return null;
			}
		}
	}

}
