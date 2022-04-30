package lovexyn0827.mess.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

/**
 * @author lovexyn0827
 * Date: April 10, 2022
 */
public class Reflection {
	public static final Map<EntityType<?>, Class<?>> ENTITY_TYPE_TO_CLASS = Maps.newHashMap();
	private static final Pattern GERERIC_TYPE_EXTRACTOR = Pattern.compile("<([0-9a-zA-Z_.]*)>");
	
	public static boolean hasField(Class<?> clazz, final Field field) {
		return hasField(clazz, field.getName());
	}
	
	public static boolean hasField(Class<?> clazz, final String field) {
		while(clazz != Object.class) {
			try {
				if(Stream.of(clazz.getDeclaredFields()).map(Field::getName).anyMatch(field::equals)) {
					return true;
				}
			} catch (SecurityException e) {}
				
			clazz = clazz.getSuperclass();
		}
	
		return false;
	}
	
	public static Set<String> getAvailableFields(Class<?> entityClass) {
		Set<String> fieldSet = new TreeSet<>();
		Mapping mapping = MessMod.INSTANCE.getMapping();
		while(entityClass != Object.class) {
			for(Field field : entityClass.getDeclaredFields()) {
				if(!mapping.isDummy()) {
					fieldSet.add(MessMod.INSTANCE.getMapping().namedField(field.getName()));
				} else {
					fieldSet.add(field.getName());
				}
			}
			
			entityClass = entityClass.getSuperclass();
		}
		
		return fieldSet;
	}
	
	/**
	 * This method could be replaced by {@code getField(Class, String)}, as the process of getting the srg name of 
	 * the field needs the name of the class declaring the field.
	 * @param fieldName Use the srg name
	 * @return A Field instance if the specified field exists in the given class or its super classes, null otherwise.
	 */
	@Nullable
	public static Field getFieldFromNamed(Class<?> targetClass, String fieldName) {
		Mapping mapping = MessMod.INSTANCE.getMapping();
		while(targetClass != null && targetClass != Object.class) {
			String srg = mapping.srgField(targetClass.getName(), fieldName);
			if(srg != null) {
				try {
					return targetClass.getDeclaredField(srg);
				} catch (NoSuchFieldException e) {
				} catch (SecurityException e) {}
			}
			
			targetClass = targetClass.getSuperclass();
		}
		
		return null;
	}
	
	private static Set<Method> listMethods(Class<?> cl, Set<Class<?>> interfaces) {
		Set<Method> set = Sets.newHashSet();
		if(interfaces == null) {
			interfaces = Sets.newHashSet();
		}
		
		do {
			for(Method m : cl.getDeclaredMethods()) {
				set.add(m);
			}
			
			for(Class<?> in : cl.getInterfaces()) {
				if(interfaces.add(cl)) {
					set.addAll(listMethods(in, interfaces));
				}
			}
		} while((cl = cl.getSuperclass()) != null);
		
		return set;
	}
	
	/**
	 * Get the methods declared by the given class {@code cl}, its super classes, and interfaces implemented by it.
	 * @param cl The srg name of the class
	 * @return A set of requested methods
	 */
	public static Set<Method> listMethods(Class<?> cl) {
		return listMethods(cl, null);
	}

	public static boolean isPrimitive(@NotNull Type type) {
		if(type instanceof Class) {
			return ((Class<?>) type).isPrimitive();
		}
		
		return false;
	}

	/**
	 * Extract the raw type from the given Type instance.
	 * @param type
	 * @return The Class instance corresponding to the raw type of {@code type}, or null if it couldn't determiner.
	 */
	@Nullable
	public static Class<?> getRawType(@NotNull Type type) {
		// TODO Check if types like List<String>[] and ? extends List & Queue can be handled properly
		if(type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType){
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if(type instanceof WildcardType) {
			Type[] bounds = ((WildcardType) type).getUpperBounds();
			return bounds.length == 0 ? null : (Class<?>) bounds[0];
		} else {
			return null;
		}
	}

	static {
		Stream.of(EntityType.class.getFields())
				.filter((f) -> Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()))
				.forEach((f) -> {
					try {
						Matcher m = GERERIC_TYPE_EXTRACTOR.matcher(f.toGenericString());
						if(m.find() && f.getType() == EntityType.class) {
							Class<?> cl = Class.forName(m.group(1));
							if(Entity.class.isAssignableFrom(cl)) {
								ENTITY_TYPE_TO_CLASS.put((EntityType<?>) f.get(null), cl);
							}
						}
					} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				});
	}
}
