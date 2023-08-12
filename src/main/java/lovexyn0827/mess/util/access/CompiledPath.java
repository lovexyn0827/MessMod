package lovexyn0827.mess.util.access;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.MethodDescriptor;
import lovexyn0827.mess.util.Reflection;
import net.minecraft.util.Util;

public abstract class CompiledPath implements AccessingPath {
	public static final Handle METHOD_BSM_HANDLE = Util.make(() -> {
		Method bsm;
		try {
			bsm = CompiledPath.class.getDeclaredMethod("methodBSM", 
					MethodHandles.Lookup.class, String.class, MethodType.class, 
					String.class, String.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new Handle(Opcodes.H_INVOKESTATIC, org.objectweb.asm.Type.getInternalName(CompiledPath.class), 
				"methodBSM", org.objectweb.asm.Type.getMethodDescriptor(bsm), false);
	});
	public static final Handle FIELD_BSM_HANDLE = Util.make(() -> {
		Method bsm;
		try {
			bsm = CompiledPath.class.getDeclaredMethod("fieldBSM", 
					MethodHandles.Lookup.class, String.class, MethodType.class, String.class, String.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new Handle(Opcodes.H_INVOKESTATIC, org.objectweb.asm.Type.getInternalName(CompiledPath.class), 
				"fieldBSM", org.objectweb.asm.Type.getMethodDescriptor(bsm), false);
	});
	public static final int IN_DY_GETTER = 0;
	public static final int IN_DY_SETTER = 1;
	protected final String name;
	
	protected CompiledPath(String name) {
		this.name = name;
	}

	@Override
	public Object access(Object start, Type inputType) throws AccessingFailureException {
		throw new IllegalStateException("access() method was not generated!");
	}

	@Override
	public void write(Object start, Type genericType, String valueStr)
			throws AccessingFailureException, CommandSyntaxException {
		// TODO
		throw new UnsupportedOperationException("Writing not implemented");
	}

	@Override
	public Type getOutputType() {
		return Object.class;
	}

	@Override
	public Class<?> compile(List<Class<?>> nodeInputTypes, String name) throws CompilationException {
		return this.getClass();
	}
	
	// MethodHandle constants is not used as it may cause failures in access checking process.
	protected static CallSite methodBSM(MethodHandles.Lookup lookup, String name, MethodType type, 
			String className, String methodName, String descriptor) {
		Class<?> cl;
		try {
			cl = Class.forName(className.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		MethodDescriptor descObj = MethodDescriptor.parse(
				MessMod.INSTANCE.getMapping().srgMethodDescriptor(descriptor));
		Method m = Reflection.getMethodFromDesc(cl, methodName, descObj);
		if(m == null) {
			throw new NoSuchMethodError(String.format("Failed to locate method: %s.%s%s", 
					className, methodName, descriptor));
		}
		
		m.setAccessible(true);
		try {
			return new ConstantCallSite(MethodHandles.lookup().unreflect(m));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	// .ibid
	protected static CallSite fieldBSM(MethodHandles.Lookup lookup, String name, MethodType type, 
			String className, String fieldName, int mode) {
		Class<?> cl;
		try {
			cl = Class.forName(className.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		Field f;
		try {
			f = cl.getDeclaredField(fieldName);
		} catch (SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		f.setAccessible(true);
		MethodHandle mh;
		try {
			switch(mode) {
			case IN_DY_GETTER: 
				mh = MethodHandles.lookup().unreflectGetter(f);
				break;
			case IN_DY_SETTER: 
				mh = MethodHandles.lookup().unreflectSetter(f);
				break;
			default:
				throw new IllegalArgumentException("Invalid mode: " + mode);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return new ConstantCallSite(mh);
	}
	
	protected static Function<?, ?>[] parseLambdas(Class<?> thisClass) {
		List<Function<?, ?>> parsed = new ArrayList<>();
		byte[] bytes = thisClass.getAnnotation(Lambdas.class).bytes();
		try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			int count = ois.readInt();
			for(int i = 0; i < count; i++) {
				parsed.add((Function<?, ?>) ois.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return parsed.toArray(new Function<?, ?>[parsed.size()]);
	}

	@Override
	public String getOriginalStringRepresentation() {
		return this.name;
	}
	
	/** 
	 * Used in classes of paths to store serialized lambda expressions.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Lambdas {
		byte[] bytes();
	}
}
