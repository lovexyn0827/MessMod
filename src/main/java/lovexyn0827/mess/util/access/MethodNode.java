package lovexyn0827.mess.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.InsnList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.MethodDescriptor;
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
	@Nullable
	private final Class<?> returnTypes;
	
	MethodNode(String name, String types, String args) {
		this.name = name;
		if(types != null) {
			if(types.matches("[0-9]+")) {
				this.types = null;
				this.returnTypes = null;
				try {
					this.argNum = Integer.parseInt(types);
				} catch (NumberFormatException e) {
					throw new TranslatableException("exp.invaildInvocation", this);
				}
			} else {
				this.argNum = null;
				MethodDescriptor desc = MethodDescriptor.parse(
						MessMod.INSTANCE.getMapping().srgMethodDescriptor(types));
				this.types = desc.argTypes;
				this.returnTypes = desc.returnType;
			}
		} else {
			this.types = null;
			this.argNum = null;
			this.returnTypes = null;
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
				try {
					argObjs[i] = argsL[i].get(argTypes[i]);	// XXX Generic type
				} catch (InvalidLiteralException e) {
					throw AccessingFailureException.create(e, this);
				}
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
			if(OptionManager.superSuperSecretSetting) {
				e.printStackTrace();
				MessMod.LOGGER.info("Failed to invoke " + this.method);
			}
			throw AccessingFailureException.createWithArgs(FailureCause.ERROR, this, e, e);
		}
	}
	
	private void resolveAndSetMethod(Class<? extends Object> clazz) throws AccessingFailureException {
		Mutable<String> srg = new MutableObject<>();
		Mapping map = MessMod.INSTANCE.getMapping();
		final List<Method> candidates = Reflection.listMethods(clazz).stream()
				.filter((m) -> {
					// Determine whether or not a method can be a valid candidate.
					// FIXME When multiple methods has the same types of parameters, the method be chosen inaccurately
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
				.map(Reflection::getDeepestOverridenMethod)
				.distinct()
				.collect(Collectors.toList());
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
	void initialize(Type lastOutType) throws AccessingFailureException  {
		Class<?> cl = Reflection.getRawType(lastOutType);
		this.resolveAndSetMethod(cl);
		super.initialize(lastOutType);
	}

	@Override
	protected Type resolveOutputType(Type lastOutType){
		return this.method.getGenericReturnType();
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

	@Override
	NodeCompiler getCompiler() {
		// TODO The same as MapperNode.getCompiler()?
		return (ctx) -> {
			InsnList insns = new InsnList();
			if(this.method == null) {
				throw new CompilationException(FailureCause.ERROR);
			} else {
				// 1. Prepare arguments
				int argsLength = this.args.length;
				Class<?>[] argTypes = this.method.getParameterTypes();
				for(int i = 0; i < argsLength; i++) {
					Literal<?> literal = this.args[i];
					BytecodeHelper.appendConstantLoader(ctx, insns, literal, argTypes[i]);
				}
				
				// 2. Invoke underlying method
				BytecodeHelper.appendCaller(insns, this.method, CompilationContext.CallableType.INVOKER);
			}
			
			ctx.endNode(this.method.getGenericReturnType());
			return insns;
		};
	}
}
