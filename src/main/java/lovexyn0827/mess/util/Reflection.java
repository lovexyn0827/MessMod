package lovexyn0827.mess.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.bytes.Byte2BooleanMap;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.Byte2CharMap;
import it.unimi.dsi.fastutil.bytes.Byte2DoubleMap;
import it.unimi.dsi.fastutil.bytes.Byte2FloatMap;
import it.unimi.dsi.fastutil.bytes.Byte2IntMap;
import it.unimi.dsi.fastutil.bytes.Byte2LongMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ShortMap;
import it.unimi.dsi.fastutil.chars.Char2BooleanMap;
import it.unimi.dsi.fastutil.chars.Char2ByteMap;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2DoubleMap;
import it.unimi.dsi.fastutil.chars.Char2FloatMap;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2LongMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ShortMap;
import it.unimi.dsi.fastutil.doubles.Double2BooleanMap;
import it.unimi.dsi.fastutil.doubles.Double2ByteMap;
import it.unimi.dsi.fastutil.doubles.Double2CharMap;
import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;
import it.unimi.dsi.fastutil.doubles.Double2FloatMap;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2LongMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ShortMap;
import it.unimi.dsi.fastutil.floats.Float2BooleanMap;
import it.unimi.dsi.fastutil.floats.Float2ByteMap;
import it.unimi.dsi.fastutil.floats.Float2CharMap;
import it.unimi.dsi.fastutil.floats.Float2DoubleMap;
import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import it.unimi.dsi.fastutil.floats.Float2LongMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2CharMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2CharMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2ByteMap;
import it.unimi.dsi.fastutil.objects.Reference2CharMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ByteMap;
import it.unimi.dsi.fastutil.shorts.Short2CharMap;
import it.unimi.dsi.fastutil.shorts.Short2DoubleMap;
import it.unimi.dsi.fastutil.shorts.Short2FloatMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2LongMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;

/**
 * @author lovexyn0827
 * Date: April 10, 2022
 */
public class Reflection {
	public static final Map<EntityType<?>, Class<?>> ENTITY_TYPE_TO_CLASS;
	private static final ImmutableMap<String, Class<?>> PRIMITIVE_CLASSES;
	public static final ImmutableBiMap<BlockEntityType<?>, Class<?>> BLOCK_ENTITY_TYPE_TO_CLASS;
	// Map class => {Key class, Value class} (type arguments are represented by null)
	public static final ImmutableMap<Class<?>, Pair<Class<?>, Class<?>>> MAP_TO_TYPES;
	
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
	 * @return The Class instance corresponding to the raw type of {@code type}, or {@code Object.class} if not determined.
	 */
	@NotNull
	public static Class<?> getRawType(@NotNull Type type) {
		// TODO Check if types like List<String>[] and ? extends List & Queue can be handled properly
		if(type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType){
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if(type instanceof WildcardType) {
			Type[] bounds = ((WildcardType) type).getUpperBounds();
			return bounds.length == 0 ? Object.class : (Class<?>) bounds[0];
		} else {
			return Object.class;
		}
	}
	
	public static Class<?> getClassIncludingPrimitive(String name) throws ClassNotFoundException {
		Class<?> priC = PRIMITIVE_CLASSES.get(name);
		if(priC != null) {
			return priC;
		} else {
			return Class.forName(name);
		}
	}
	
	/**
	 * Gets a method that is overridden by the given one. If two different {@code in} overrides the same method,
	 * the returned method should be identical whenever possible.
	 */
	@NotNull
	public static Method getDeepestOverridenMethod(Method in) {
		return getAllMethods(in.getDeclaringClass()).stream()
				.filter((m) -> isOverriding(m, in))
				.sorted((m1, m2) -> m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass()) ? -1 : 1)
				.findFirst()
				.orElseGet(() -> in);
	}
	
	public static boolean isOverriding(Method m, Method maySuper) {
		if (m.getName().equals(maySuper.getName()) 
				&& m.getParameterCount() == maySuper.getParameterCount()) {
			if(!maySuper.getReturnType().isAssignableFrom(m.getReturnType())) {
				return false;
			}
			
			int argCount = m.getParameterCount();
			Class<?>[] mTypes = m.getParameterTypes();
			Class<?>[] maySuperTypes = maySuper.getParameterTypes();
			for(int i = 0; i < argCount; i++) {
				if(!mTypes[i].isAssignableFrom(maySuperTypes[i])) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}

	public static boolean isOverriding(Executable m, Executable maySuper) {
		if((m instanceof Method) && (maySuper instanceof Method)) {
			return isOverriding((Method) m, (Method) maySuper);
		} else if((m instanceof Constructor) && (maySuper instanceof Constructor)) {
			return isOverriding((Constructor<?>) m, (Constructor<?>)maySuper);
		} else {
			return false;
		}
	}

	private static boolean isOverriding(Constructor<?> m, Constructor<?> maySuper) {
		if (m.getParameterCount() == maySuper.getParameterCount()) {
			if(!maySuper.getDeclaringClass().isAssignableFrom(m.getDeclaringClass())) {
				return false;
			}
			
			int argCount = m.getParameterCount();
			Class<?>[] mTypes = m.getParameterTypes();
			Class<?>[] maySuperTypes = maySuper.getParameterTypes();
			for(int i = 0; i < argCount; i++) {
				if(!mTypes[i].isAssignableFrom(maySuperTypes[i])) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}

	public static Set<Class<?>> getAllInterfaces(Class<?> cl) {
		HashSet<Class<?>> set = Sets.newHashSet();
		for(Class<?> current = cl; current != null; current = current.getSuperclass()) {
			appendAllInterfacesInternal(current, set);
		}
		
		return set;
	}
	
	private static void appendAllInterfacesInternal(Class<?> cl, HashSet<Class<?>> set) {
		Class<?>[] interfaces = cl.getInterfaces();
		for(Class<?> inf : interfaces) {
			set.add(inf);
			appendAllInterfacesInternal(inf, set);
		}
	}

	public static Set<Type> getAllGenericInterfaces(Class<?> cl) {
		HashSet<Type> set = Sets.newHashSet();
		for(Class<?> current = cl; current != null; current = current.getSuperclass()) {
			appendAllGenericInterfacesInternal(current, set);
		}
		
		return set;
	}
	
	private static void appendAllGenericInterfacesInternal(Type cl, HashSet<Type> set) {
		Type[] interfaces = getRawType(cl).getGenericInterfaces();
		for(Type inf : interfaces) {
			set.add(inf);
			appendAllGenericInterfacesInternal(inf, set);
		}
	}

	public static boolean isClassExisting(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static Class<?> getTypeArgOrObject(Type inType, int ordinal) {
		if(inType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) inType;
			return getRawType(pt.getActualTypeArguments()[ordinal]);
		} else {
			return Object.class;
		}
	}
	
	/**
	 * Gets a Set containing methods of a given class, including inherited ones.
	 */
	public static Set<Method> getAllMethods(Class<?> cl) {
		HashSet<Method> set = Sets.newHashSet();
		appendAllMethodsInternal(cl, set);
		return set;
	}

	private static void appendAllMethodsInternal(Class<?> cl, HashSet<Method> set) {
		for(Method m : cl.getDeclaredMethods()) {
			set.add(m);
		}
		
		for(Class<?> in : cl.getInterfaces()) {
			appendAllMethodsInternal(in, set);
		}
		
		Class<?> superCl = cl.getSuperclass();
		if(superCl != null) {
			appendAllMethodsInternal(superCl, set);
		} else {
			return;
		}
	}

	@Nullable
	public static Class<?> toClassOrNull(org.objectweb.asm.Type type, boolean shouldMapToSrg) {
		String name;
		if(shouldMapToSrg) {
			Mapping map = MessMod.INSTANCE.getMapping();
			name = type.getSort() == org.objectweb.asm.Type.ARRAY ? 
					map.srgDescriptor(type.getDescriptor()) : map.srgClass(type.getClassName());
		} else {
			name = type.getSort() == org.objectweb.asm.Type.ARRAY ? type.getDescriptor() : type.getClassName();
		}
		
		try {
			return getClassIncludingPrimitive(name.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Gets a method with <b>exactly</b> matching name and descriptor, from a given class and its super types.
	 */
	@Nullable
	public static Method getMethodFromDesc(Class<?> cl, String name, MethodDescriptor desc) {
		for(Method m : getAllMethods(cl)) {
			if(name.equals(m.getName())) {
				if(desc.matches(m)) {
					return m;
				}
			}
		}
		
		return null;
	}

	public static Method getMethodForInternalPropose(Class<?> owner, String name, String srg, Class<?> ... argTypes) {
		try {
			if(FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named")) {
				return owner.getDeclaredMethod(name, argTypes);
			} else {
				return owner.getDeclaredMethod(srg, argTypes);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	static {
		ImmutableBiMap.Builder<EntityType<?>, Class<?>> entityTypeMapBuilder = ImmutableBiMap.builder();
		Stream.of(EntityType.class.getFields())
				.filter((f) -> {
					return Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
							&& f.getType() == EntityType.class;
				}).forEach((f) -> {
					try {
						Type fType = f.getGenericType();
						if(fType instanceof ParameterizedType) {
							ParameterizedType genericType = (ParameterizedType) fType;
							Type[] typeArgs = genericType.getActualTypeArguments();
							if(typeArgs.length == 1) {
								entityTypeMapBuilder.put((EntityType<?>) f.get(null), getRawType(typeArgs[0]));
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		ENTITY_TYPE_TO_CLASS = entityTypeMapBuilder.build();
		ImmutableBiMap.Builder<BlockEntityType<?>, Class<?>> beTypeMapBuilder = ImmutableBiMap.builder();
		Stream.of(BlockEntityType.class.getFields())
				.filter((f) -> {
					return Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
							&& f.getType() == BlockEntityType.class;
				})
				.forEach((f) -> {
					try {
						Type fType = f.getGenericType();
						if(fType instanceof ParameterizedType) {
							ParameterizedType genericType = (ParameterizedType) fType;
							Type[] typeArgs = genericType.getActualTypeArguments();
							if(typeArgs.length == 1) {
								beTypeMapBuilder.put((BlockEntityType<?>) f.get(null), getRawType(typeArgs[0]));
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		BLOCK_ENTITY_TYPE_TO_CLASS = beTypeMapBuilder.build();
		PRIMITIVE_CLASSES = ImmutableMap.<String, Class<?>>builder()
				.put("int", int.class)
				.put("long", long.class)
				.put("float", float.class)
				.put("double", double.class)
				.put("boolean", boolean.class)
				.put("short", short.class)
				.put("byte", byte.class)
				.put("void", void.class)
				.build();
		MAP_TO_TYPES = ImmutableMap
				.<Class<?>, Pair<Class<?>, Class<?>>>builder()
				.put(Object2DoubleMap.class, new Pair<>(null, double.class))
				.put(Object2IntMap.class, new Pair<>(null, int.class))
				.put(Object2FloatMap.class, new Pair<>(null, float.class))
				.put(Object2LongMap.class, new Pair<>(null, long.class))
				.put(Object2BooleanMap.class, new Pair<>(null, boolean.class))
				.put(Object2CharMap.class, new Pair<>(null, char.class))
				.put(Object2ByteMap.class, new Pair<>(null, byte.class))
				.put(Object2ShortMap.class, new Pair<>(null, short.class))
				.put(Reference2DoubleMap.class, new Pair<>(null, double.class))
				.put(Reference2IntMap.class, new Pair<>(null, int.class))
				.put(Reference2FloatMap.class, new Pair<>(null, float.class))
				.put(Reference2LongMap.class, new Pair<>(null, long.class))
				.put(Reference2BooleanMap.class, new Pair<>(null, boolean.class))
				.put(Reference2CharMap.class, new Pair<>(null, char.class))
				.put(Reference2ByteMap.class, new Pair<>(null, byte.class))
				.put(Reference2ShortMap.class, new Pair<>(null, short.class))
				.put(Int2DoubleMap.class, new Pair<>(int.class, double.class))
				.put(Int2IntMap.class, new Pair<>(int.class, int.class))
				.put(Int2FloatMap.class, new Pair<>(int.class, float.class))
				.put(Int2LongMap.class, new Pair<>(int.class, long.class))
				.put(Int2BooleanMap.class, new Pair<>(int.class, boolean.class))
				.put(Int2CharMap.class, new Pair<>(int.class, char.class))
				.put(Int2ByteMap.class, new Pair<>(int.class, byte.class))
				.put(Int2ShortMap.class, new Pair<>(int.class, short.class))
				.put(Double2DoubleMap.class, new Pair<>(double.class, double.class))
				.put(Double2IntMap.class, new Pair<>(double.class, int.class))
				.put(Double2FloatMap.class, new Pair<>(double.class, float.class))
				.put(Double2LongMap.class, new Pair<>(double.class, long.class))
				.put(Double2BooleanMap.class, new Pair<>(double.class, boolean.class))
				.put(Double2CharMap.class, new Pair<>(double.class, char.class))
				.put(Double2ByteMap.class, new Pair<>(double.class, byte.class))
				.put(Double2ShortMap.class, new Pair<>(double.class, short.class))
				.put(Float2DoubleMap.class, new Pair<>(float.class, double.class))
				.put(Float2IntMap.class, new Pair<>(float.class, int.class))
				.put(Float2FloatMap.class, new Pair<>(float.class, float.class))
				.put(Float2LongMap.class, new Pair<>(float.class, long.class))
				.put(Float2BooleanMap.class, new Pair<>(float.class, boolean.class))
				.put(Float2CharMap.class, new Pair<>(float.class, char.class))
				.put(Float2ByteMap.class, new Pair<>(float.class, byte.class))
				.put(Float2ShortMap.class, new Pair<>(float.class, short.class))
				.put(Long2DoubleMap.class, new Pair<>(long.class, double.class))
				.put(Long2IntMap.class, new Pair<>(long.class, int.class))
				.put(Long2FloatMap.class, new Pair<>(long.class, float.class))
				.put(Long2LongMap.class, new Pair<>(long.class, long.class))
				.put(Long2BooleanMap.class, new Pair<>(long.class, boolean.class))
				.put(Long2CharMap.class, new Pair<>(long.class, char.class))
				.put(Long2ByteMap.class, new Pair<>(long.class, byte.class))
				.put(Long2ShortMap.class, new Pair<>(long.class, short.class))
				.put(Char2DoubleMap.class, new Pair<>(char.class, double.class))
				.put(Char2IntMap.class, new Pair<>(char.class, int.class))
				.put(Char2FloatMap.class, new Pair<>(char.class, float.class))
				.put(Char2LongMap.class, new Pair<>(char.class, long.class))
				.put(Char2BooleanMap.class, new Pair<>(char.class, boolean.class))
				.put(Char2CharMap.class, new Pair<>(char.class, char.class))
				.put(Char2ByteMap.class, new Pair<>(char.class, byte.class))
				.put(Char2ShortMap.class, new Pair<>(char.class, short.class))
				.put(Byte2DoubleMap.class, new Pair<>(byte.class, double.class))
				.put(Byte2IntMap.class, new Pair<>(byte.class, int.class))
				.put(Byte2FloatMap.class, new Pair<>(byte.class, float.class))
				.put(Byte2LongMap.class, new Pair<>(byte.class, long.class))
				.put(Byte2BooleanMap.class, new Pair<>(byte.class, boolean.class))
				.put(Byte2CharMap.class, new Pair<>(byte.class, char.class))
				.put(Byte2ByteMap.class, new Pair<>(byte.class, byte.class))
				.put(Byte2ShortMap.class, new Pair<>(byte.class, short.class))
				.put(Short2DoubleMap.class, new Pair<>(short.class, double.class))
				.put(Short2IntMap.class, new Pair<>(short.class, int.class))
				.put(Short2FloatMap.class, new Pair<>(short.class, float.class))
				.put(Short2LongMap.class, new Pair<>(short.class, long.class))
				.put(Short2BooleanMap.class, new Pair<>(short.class, boolean.class))
				.put(Short2CharMap.class, new Pair<>(short.class, char.class))
				.put(Short2ByteMap.class, new Pair<>(short.class, byte.class))
				.put(Short2ShortMap.class, new Pair<>(short.class, short.class))
				.put(Double2ObjectMap.class, new Pair<>(double.class, null))
				.put(Int2ObjectMap.class, new Pair<>(int.class, null))
				.put(Float2ObjectMap.class, new Pair<>(float.class, null))
				.put(Long2ObjectMap.class, new Pair<>(long.class, null))
				.put(Char2ObjectMap.class, new Pair<>(char.class, null))
				.put(Byte2ObjectMap.class, new Pair<>(byte.class, null))
				.put(Short2ObjectMap.class, new Pair<>(short.class, null))
				.build();
	}
}
