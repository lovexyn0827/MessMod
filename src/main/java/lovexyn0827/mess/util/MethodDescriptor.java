package lovexyn0827.mess.util;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.objectweb.asm.Type;

public final class MethodDescriptor {
	public final String stringForm;
	public final Type asmType;
	public final Class<?> returnType;
	public final Class<?>[] argTypes;
	public final Type returnAsmType;
	public final Type[] argAsmTypes;
	
	private MethodDescriptor(String stringForm, Type asmType, Class<?> returnType, Class<?>[] argTypes, 
			Type returnAsmType, Type[] argAsmTypes) {
		this.stringForm = stringForm;
		this.asmType = asmType;
		this.argTypes = argTypes;
		this.returnType = returnType;
		this.returnAsmType = returnAsmType;
		this.argAsmTypes = argAsmTypes;
	}
	
	public static MethodDescriptor parse(String desc) {
		try{
			return of(Type.getMethodType(desc));
		} catch (RuntimeException e) {
			throw new TranslatableException("exp.descriptor");
		}
	}
	
	public static MethodDescriptor of(Method m) {
		try{
			return of(Type.getType(m));
		} catch (RuntimeException e) {
			throw new TranslatableException("exp.descriptor");
		}
	}
	
	public static MethodDescriptor of(Type asmType) {
		Type[] argsAT = asmType.getArgumentTypes();
		Class<?>[] argsT = new Class<?>[argsAT.length];
		for(int i = 0; i < argsAT.length; i++) {
			argsT[i] = toClassOrThrow(argsAT[i]);
		}
		
		Type returnAT = asmType.getReturnType();
		return new MethodDescriptor(asmType.getDescriptor(), asmType, toClassOrThrow(returnAT), 
				argsT, returnAT, argsAT);
	}
	
	private static Class<?> toClassOrThrow(Type type) {
		Class<?> argI = Reflection.toClassOrNull(type, true);
		if(argI != null) {
			return argI;
		} else {
			TranslatableException e1 = new TranslatableException("exp.noclass", type.getClassName());
			throw e1;
		}
	}
	
	public boolean matches(Method m) {
		return Arrays.equals(this.argTypes, m.getParameterTypes()) && m.getReturnType().equals(this.returnType);
	}
	
	/**
	 * @return {@code true} if m can be overridden by a method with this descriptor.
	 */
	public boolean canBeOverriderOf(Method m) {
		int argCount = this.argTypes.length;
		if(m.getParameterCount() == argCount) {
			return false;
		} else {
			Class<?>[] mArgs = m.getParameterTypes();
			for(int i = 0; i < argCount; i++) {
				if(!mArgs[i].isAssignableFrom(this.argTypes[i])) {
					return false;
				}
			}
			
			return this.returnType.isAssignableFrom(m.getReturnType());
		}
	}
	
	/**
	 * @return {@code true} if m can override a method with this descriptor.
	 */
	public boolean canBeOverridenBy(Method m) {
		int argCount = this.argTypes.length;
		if(m.getParameterCount() == argCount) {
			return false;
		} else {
			Class<?>[] mArgs = m.getParameterTypes();
			for(int i = 0; i < argCount; i++) {
				if(!this.argTypes[i].isAssignableFrom(mArgs[i])) {
					return false;
				}
			}
			
			return m.getReturnType().isAssignableFrom(this.returnType);
		}
	}
	
	@Override
	public String toString() {
		return this.stringForm;
	}
}
