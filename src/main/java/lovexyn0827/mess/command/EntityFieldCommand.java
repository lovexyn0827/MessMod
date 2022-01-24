package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.deobfuscating.DummyMapping;
import lovexyn0827.mess.deobfuscating.Mapping;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class EntityFieldCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggests = (ct,builder)->{
			for(String fieldName : getAvailableFields(EntityArgumentType.getEntity(ct, "target"))) {
				builder = builder.suggest(fieldName);
			}
			return builder.buildFuture();
		};
		
		LiteralArgumentBuilder<ServerCommandSource> get = literal("get").
				then(argument("fieldName",StringArgumentType.string()).suggests(suggests).
						executes((ct)->{
							try {
								Entity entity = EntityArgumentType.getEntity(ct, "target");
								Field field =getField(entity.getClass(),StringArgumentType.getString(ct, "fieldName"));
								if(field != null) {
									CommandUtil.feedback(ct, field == null ? "[NULL]" : field.get(entity));
								} else {
									CommandUtil.error(ct, "Specified field coundn't be found!");
								}
							} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
								CommandUtil.error(ct, "Unexpected exception in getting the field:" + e);
								return -1;
							} catch (NoSuchFieldError e) {
								CommandUtil.error(ct, "No such field in the class of the entity");
							}
							
							return 0;
						}));
		
		ArgumentBuilder<ServerCommandSource, ?> modify = literal("modify").requires((source)->source.hasPermissionLevel(1)).
				then(argument("fieldName",StringArgumentType.string()).suggests(suggests).
						then(argument("newValue",StringArgumentType.string()).
								executes((ct)->{
									try {
										modifyField(EntityArgumentType.getEntity(ct, "target"),
												StringArgumentType.getString(ct, "fieldName"),
												StringArgumentType.getString(ct, "newValue"));
										CommandUtil.feedback(ct, "Field was modified successfully");
										return 1;
									} catch (Exception e) {
										CommandUtil.error(ct, "Failed to modify the field:" + e);
										return -1;
									}
								})));
		
		ArgumentBuilder<ServerCommandSource, ?> listAll = literal("listAvailableFields").
				executes((ct)->{
					Set<String> fieldSet;
					try {
						fieldSet = getAvailableFields(EntityArgumentType.getEntity(ct, "target"));
						String list = String.join("  ", fieldSet);
						CommandUtil.feedback(ct, list);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					return 0;
				});
		
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entityfield").requires((source)->source.hasPermissionLevel(1)).
				then(argument("target",EntityArgumentType.entity()).then(get).then(modify).then(listAll));
		dispatcher.register(command);
	}
	
	private static String getNamedField(String obfuscated) {
		return MessMod.INSTANCE.mapping.namedField(obfuscated);
	}
	
	/*private static String getSrgField(String clazz, String named) {
		return MessMod.INSTANCE.mapping.srgField(clazz, named);
	}*/

	private static boolean modifyField(Entity entity, String fieldName, String newValue) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Field field = getField(entity.getClass(), fieldName);
		if(field == null) {
			throw new IllegalArgumentException("Field was not found in class" + entity.getClass());
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
			if(subVals.length!=3) throw new IllegalArgumentException("Too many or too few components given!");
			Vec3d vec3d = new Vec3d(Double.parseDouble(subVals[0]),
					Double.parseDouble(subVals[1]),
					Double.parseDouble(subVals[2]));
			field.set(entity, vec3d);
		} else if (type == Boolean.TYPE){
			if((!newValue.equals("true")) && (!newValue.equals("false"))) throw new IllegalArgumentException("Use true or false");
			field.set(entity, Boolean.parseBoolean(newValue));
		} else {
			throw new IllegalArgumentException("Unsupported field given!");
		}
		
		return true;
	}

	private static Set<String> getAvailableFields(Entity entity) {
		Class<?> entityClass = entity.getClass();
		Set<String> fieldSet = new TreeSet<>();
		Mapping mapping = MessMod.INSTANCE.getMapping();
		while(entityClass != Object.class) {
			for(Field field : entityClass.getDeclaredFields()) {
				if(!(mapping instanceof DummyMapping)) {
					fieldSet.add(getNamedField(field.getName()));
				} else {
					fieldSet.add(field.getName());
				}
			}
			
			entityClass = entityClass.getSuperclass();
		}
		
		return fieldSet;
	}

	private static Field getField(Class<?> targetClass, String fieldName) {
		Mapping mapping = MessMod.INSTANCE.getMapping();
		if(!(mapping instanceof DummyMapping)) {
			fieldName = mapping.srgFieldRecursively(targetClass, fieldName);
		}
		
		while(true) {
			try {
				Field field = targetClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch(NoSuchFieldException e) {
				if(!Object.class.equals(targetClass)) {
					targetClass = targetClass.getSuperclass();
					continue;
				}
				
				return null;
			}
		}
	}

}
