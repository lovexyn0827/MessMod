package lovexyn0827.mess.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.deobfuscating.Mapping;

final class MethodNode extends Node implements Cloneable {
	static final Pattern METHOD_PATTERN = Pattern.compile(
			"^(?<name>[$_a-zA-Z0-9]+)(?:\\<(?<types>[^>]*)\\>)?\\((?<args>.*)\\)$");

	private final String name;
	@Nullable
	private final Class<?>[] types;
	private final Literal<?>[] args;
	@Nullable
	private Method method;
	private final Integer argNum;
	
	MethodNode(String name, String types, String args) {
		this.name = name;
		if(types != null) {
			if(types.matches("[0-9]+")) {
				this.types = null;
				try {
					this.argNum = Integer.parseInt(types);
				} catch (NumberFormatException e) {
					throw new TranslatableException("exp.invaildInvocation", this);
				}
			} else {
				this.argNum = null;
				this.types = parseDescriptor(types);
			}
		} else {
			this.types = null;
			this.argNum = null;
		}
		
		try {
			this.args = parseArgs(args);
		} catch (CommandSyntaxException e) {
			TranslatableException e1 = new TranslatableException("exp.invaildInvocation", this);
			e1.initCause(e);
			throw e1;
		}
	}
	
	@Override
	Object access(Object previous) throws AccessingFailureException {
		this.ensureInitialized();
		try {
			this.method.setAccessible(true);
			Literal<?>[] argsL = this.args;
			Object[] argObjs = new Object[argsL.length];
			Type[] argTypes = this.method.getGenericParameterTypes();
			for(int i = 0; i < argsL.length; i++) {
				argObjs[i] = argsL[i].get(argTypes[i]);	// XXX Generic type
			}

			try {
				return this.method.invoke(previous, argObjs);
			} catch (IllegalArgumentException e) {
				throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
						Arrays.toString(argObjs), this.method.toString());
			}
		} catch (InvocationTargetException e) {
			
			throw AccessingFailureException.createWithArgs(FailureCause.INVOKE_FAIL, this, e.getCause(), 
					this.name, e.getCause());
		} catch (AccessingFailureException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			MessMod.LOGGER.info("Failed to invoke " + this.method);
			throw AccessingFailureException.createWithArgs(FailureCause.ERROR, this, e, e);
		}
	}
	
	private void resolveMethod(Class<? extends Object> clazz) throws AccessingFailureException {
		Mutable<String> srg = new MutableObject<>();
		Mapping map = MessMod.INSTANCE.getMapping();
		final List<Method> candidates = Lists.newArrayList();
		Reflection.listMethods(clazz).stream()
				.filter((m) -> {
					String descriptor = org.objectweb.asm.Type.getMethodDescriptor(m);
					srg.setValue(map.srgMethodRecursively(clazz, this.name, descriptor));
					if(m.getName().equals(srg.getValue())) {
						if(this.argNum != null) {
							return this.argNum.equals(m.getParameterCount());
						} else if (this.types != null) {
							return Arrays.equals(this.types, m.getParameterTypes());
						} else {
							return true;
						}
					} else {
						return false;
					}
				})
				.filter((m) -> !m.isSynthetic())
				.forEach((target) -> {
					boolean hasTheSame = false;
					for(Method m : candidates) {
						if(Arrays.equals(m.getParameterTypes(), target.getParameterTypes())
								&& m.getReturnType().equals(target.getReturnType())) {
							hasTheSame = true;
							break;
						}
					}
					
					if(!hasTheSame) {
						candidates.add(target);
					}
				});
		if(candidates.size() == 1) {
			this.method = candidates.get(0);
		} else if(candidates.size() == 0) {
			throw AccessingFailureException.createWithArgs(FailureCause.NO_METHOD, this, null, 
					srg.getValue(), clazz.getSimpleName());	// XXX Deobfusciation
		} else {
			throw AccessingFailureException.create(FailureCause.MULTI_TARGET, this);
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
		return (this.method != null ? this.method.equals(other.method) : this.name.equals(other.name)) 
				&& (this.outputType == null ? other.outputType == null : this.outputType.equals(other.outputType))
				&& Arrays.equals(this.args, args)
				&& Arrays.equals(this.types, this.types);
	}
	
	@Override
	public String toString() {
		String types;
		if(this.argNum != null) {
			types = '<' + this.argNum.toString() + '>';
		} else if (this.types != null) {
			StringBuilder sb = new StringBuilder("<");
			for(Class<?> cl : this.types) {
				sb.append(org.objectweb.asm.Type.getDescriptor(cl));
			}
			
			sb.append('>');
			types = sb.toString();
		} else {
			types = "";
		}
		
		StringBuilder sb2 = new StringBuilder("(");
		if (this.args != null) {
			for (Literal<?> l : this.args) {
				sb2.append(l.stringRepresentation);
				sb2.append(',');
			}
		} else {
			sb2.append("???");
		}
		
		sb2.append(')');
		return this.name + types + sb2;
	}
	
	@Override
	boolean canFollow(Node n) {
		return n.outputType != null && Reflection.listMethods(
				Reflection.getRawType(n.outputType)).contains(this.method);
	}

	@Override
	protected Type prepare(Type lastOutType) throws AccessingFailureException {
		Class<?> cl = Reflection.getRawType(lastOutType);
		this.resolveMethod(cl == null ? Object.class : cl);
		this.outputType = this.method.getGenericReturnType();
		return this.outputType;
	}

	public static Class<?>[] parseDescriptor(String descriptor) {
		Mapping map = MessMod.INSTANCE.getMapping();
		org.objectweb.asm.Type[] args;
		try {
			args = org.objectweb.asm.Type.getArgumentTypes(descriptor);
		} catch (RuntimeException e) {
			throw new TranslatableException("exp.descriptor");
		}
		
		Class<?>[] result = new Class<?>[args.length];
		for(int i = 0; i < args.length; i++) {
			String clName = map.srgClass(args[i].getClassName());	// XXX Srg or named
			try {
				result[i] = Reflection.getClassIncludingPrimitive(clName);
			} catch (ClassNotFoundException e) {
				TranslatableException e1 = new TranslatableException("exp.noclass", clName);
				e1.initCause(e);
				throw e1;
			}
		}
		
		return result;
	}

	public static Literal<?>[] parseArgs(String argsStr) throws CommandSyntaxException {
		if(argsStr.isEmpty()) {
			return new Literal<?>[0];
		}
		
		String[] args = new ArgumentListTokenizer(argsStr).toArray();
		Literal<?>[] result = new Literal[args.length];
		for(int i = 0; i < args.length; i++) {
			if (!args[i].isEmpty()) {
				result[i] = Literal.parse(args[i]);
			} else {
				throw new TranslatableException("exp.emptyarg");
			}
		}
		
		return result;
	}

	@Override
	void uninitialize() {
		super.uninitialize();
		this.method = null;
		if(this.args != null) {
			for(int i = 0; i < this.args.length; i++) {
				this.args[i] = this.args[i].recreate();
			}
		}
	}

	@Override
	Node createCopyForInput(Object input) {
		MethodNode mn;
		try {
			mn = (MethodNode) this.clone();
			mn.uninitialize();
			mn.ordinary = this.ordinary;
			return mn;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
}
