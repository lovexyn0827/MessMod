package lovexyn0827.mess.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.Reflection;

class MethodNode extends Node {
	static final Pattern METHOD_PATTERN = Pattern.compile(
			"^(?<name>[$_a-zA-Z0-9]+)(?:\\{(?<types>(?:[ZBCDFIJS]|(?:L[$_a-zA-z0-9/]+[^/];))*)\\})?\\((?<args>.*)\\)$");

	private final String name;
	private final String[] types;
	private final Literal<?>[] args;
	private Method method;
	
	MethodNode(String name, String[] types, Literal<?>[] args) {
		this.name = name;
		this.types = types;
		this.args = args;
	}
	
	@Override
	Object access(Object previous) {
		try {
			this.method.setAccessible(true);
			return this.method.invoke(previous);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new TranslatableException("exp.nomethod", 
					this.name, previous.getClass().getSimpleName());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new TranslatableException("Unexcepted exception");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new TranslatableException("exp.failexec", this.name, e.toString());
		}
	}
	
	private void resolveMethod(Class<? extends Object> clazz) {
		// TODO Support for arguments
		String srg = MessMod.INSTANCE.getMapping().srgMethodRecursively(clazz, this.name, "()V");
		Optional<Method> optMethod = Reflection.listMethods(clazz).stream()
				.filter((m) -> m.getParameterCount() == 0 && m.getName().equals(srg)).findAny();
		if (optMethod.isPresent()) {
			this.method = optMethod.get();
		} else {
			throw new TranslatableException("exp.nomethod", this.name, srg, clazz.getSimpleName());
		}
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() 
				^ (this.outputType != null ? this.outputType.hashCode() : 0)
				^ Arrays.hashCode(this.args)
				^ Arrays.hashCode(this.types);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || MethodNode.class != obj.getClass()) {
			return false;
		}
		
		MethodNode other = (MethodNode) obj;
		return this.method.equals(other.method) 
				&& (this.outputType == null && other.outputType == null || this.outputType.equals(other.outputType))
				&& Arrays.equals(this.args, args)
				&& Arrays.equals(this.types, this.types);
	}
	
	@Override
	public String toString() {
		return this.method.getName() + "()";
	}
	
	@Override
	boolean canFollow(Node n) {
		return n.outputType != null && Reflection.listMethods(
				Reflection.getRawType(n.outputType)).contains(this.method);
	}

	@Override
	protected Type prepare(Type lastOutType) {
		Class<?> cl = Reflection.getRawType(lastOutType);
		this.resolveMethod(cl == null ? Object.class : cl);
		this.outputType = method.getGenericReturnType();
		return this.outputType;
	}

	public static String[] parseDescriptor(String descriptor) {
		// TODO
		return null;
	}

	public static Literal<?>[] parseArgs(String group) {
		// TODO
		return null;
	}

}
