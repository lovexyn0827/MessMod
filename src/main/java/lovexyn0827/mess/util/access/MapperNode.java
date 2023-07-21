package lovexyn0827.mess.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.deobfuscating.Mapping;

/**
 * Actually, many other nodes are also mappers, but this one is more generic.
 * @date 2022/12/27
 */
public class MapperNode extends Node {
	static final Pattern PATTERN = Pattern.compile("^(?<class>([a-zA-Z0-9_$]+/)*([a-zA-Z0-9_$]+))"
			+ "::(?<name>[$_a-zA-Z0-9]+)(?:\\<(?<types>[^>]*)\\>)?(\\((?<args>.*)\\))?$");
	private final Method method;
	// may contain null, to show that the input will be used.
	@NotNull
	private final Literal<?>[] arguments;
	private final Mode mode;
	
	public MapperNode(String toParse) throws CommandSyntaxException {
		Triple<Method, Literal<?>[], Mode> result = parse(toParse);
		this.method = result.getLeft();
		this.arguments = result.getMiddle();
		this.mode = result.getRight();
	}

	private static Triple<Method, Literal<?>[], Mode> parse(String toParse) throws CommandSyntaxException {
		//Class::method<...>(...)
		// XXX Currently, the method lookup is performed on the client, which may have some valid paths rejected.
		Matcher matcher = PATTERN.matcher(toParse);
		Method method;
		Literal<?>[] args;
		Mode mode;
		if(matcher.matches()) {
			// Spilt the string representation
			String name = matcher.group("name");
			String className = matcher.group("class").replace('/', '.');
			if(name == null && className == null) {
				throw new TranslatableException("exp.mapper.format");
			}
			
			String typesStr = matcher.group("types");
			String argsStr = matcher.group("args");
			// Search for a method that satisfies the given restrictions, or throw if it fails.
			Optional<Method> mayMethod = resolveMethod(className, name, typesStr);
			if(mayMethod.isPresent()) {
				method = mayMethod.get();
			} else {
				String mS = name + ((typesStr == null) ? "" : '<' + typesStr + ">");
				throw new TranslatableException("exp.nomethod", mS, className);
			}
			
			// 
			if(typesStr == null) {
				// Arguments are not supported in simple mode as the argument must be the input object itself
				boolean hasArgs = argsStr == null || argsStr.isEmpty();
				if(!hasArgs) {
					throw new TranslatableException("exp.mapper.unsupportedargs");
				}
				
				args = new Literal<?>[] { null };
				mode = Mode.SIMPLE;
			} else if(argsStr != null) {
				// Parse arguments
				args = parseArgs(method, argsStr);
				mode = Mode.NORMAL;
			} else {
				throw new TranslatableException("exp.mapper.format");
			}
		} else {
			throw new TranslatableException("exp.mapper.format");
		}
		
		return Triple.of(method, args, mode);
	}

	public static Literal<?>[] parseArgs(Method method, String argsStr) throws CommandSyntaxException {
		if (!argsStr.isEmpty()) {
			String[] argLS = new ArgumentListTokenizer(argsStr).toArray();
			int givenArgCount = argLS.length;
			if(givenArgCount != method.getParameterCount()) {
				throw new TranslatableException("exp.argcount", method.getParameterCount(), givenArgCount);
			}
			
			Literal<?>[] argL = new Literal<?>[givenArgCount];
			for (int i = 0; i < givenArgCount; i++) {
				String str = argLS[i];
				if ("_".equals(str)) {
					// Not necessary, but to make it more clear.
					argL[i] = null;
					continue;
				} else if (str.isEmpty()) {
					throw new TranslatableException("exp.emptyarg");
				} else {
					argL[i] = Literal.parse(str);
				}
			}
			
			return argL;
		} else {
			return new Literal<?>[0];
		}
	}
	
	private static Optional<Method> resolveMethod(String className, String name, String typesStr) {
		Mapping map = MessMod.INSTANCE.getMapping();
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			TranslatableException e1 = new TranslatableException("exp.noclass", className);
			e1.initCause(e);
			throw e1;
		}
		
		if(typesStr == null) {
			// Class::method, only methods with a single parameter and returns something.
			return Reflection.listMethods(clazz).stream()
					.filter((m) -> {
						String descriptor = org.objectweb.asm.Type.getMethodDescriptor(m);
						Class<?> declaringClass = m.getDeclaringClass();
						String srg = map.srgMethodRecursively(declaringClass, name, descriptor);
						// Matched name, single parameter, non synthetic and non void
						return m.getName().equals(srg) && m.getParameterCount() == 1 
								&& !m.isSynthetic() && m.getReturnType() != Void.TYPE;
					})
					.findFirst();
		} else {
			// Class::method<types>(args)
			if(typesStr.matches("[0-9]+")) {
				// The number of parameters is given
				int argNum = Integer.parseInt(typesStr);
				List<Method> targets = Reflection.listMethods(clazz).stream()
						.filter((m) -> m.getName().equals(name))
						.filter((m) -> m.getParameterCount() == argNum)
						.map(Reflection::getDeepestOverridenMethod)
						.distinct()
						.collect(Collectors.toList());
				if(targets.size() > 1) {
					throw new TranslatableException("exp.multitarget");
				} else if (targets.size() == 0) {
					return Optional.empty();
				} else {
					return Optional.of(targets.get(0));
				}
			} else {
				// The descriptor is given
				// TODO Handle overriding / overridden methods better.
				Class<?>[] types = MethodNode.parseDescriptor(typesStr);
				return Reflection.listMethods(clazz).stream()
						.filter((m) -> {
							String descriptor = org.objectweb.asm.Type.getMethodDescriptor(m);
							String srg = map.srgMethodRecursively(m.getDeclaringClass(), name, descriptor);
							return m.getName().equals(srg) 
									&& Arrays.equals(types, m.getParameterTypes()) 
									&& !m.isSynthetic();
						})
						.findFirst()
						.map(Reflection::getDeepestOverridenMethod);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.arguments);
		result = prime * result + Objects.hash(this.method, this.mode);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		MapperNode other = (MapperNode) obj;
		return Arrays.equals(this.arguments, other.arguments) 
				&& Objects.equals(this.method, other.method) && this.mode == other.mode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.method == null ? 
				"???" : this.method.getDeclaringClass().getCanonicalName().replace('.', '/'));
		sb.append("::");
		sb.append(this.method != null ? this.method.getName() : "???");
		if(this.mode != Mode.SIMPLE) {
			sb.append('<');
			sb.append(this.method != null ? org.objectweb.asm.Type.getMethodDescriptor(this.method) : "???");
			sb.append('>');
			sb.append('(');
			if (this.arguments != null) {
				for (Literal<?> l : this.arguments) {
					sb.append(l.stringRepresentation);
					sb.append(',');
				} 
			} else {
				sb.append("???");
			}
			sb.append(')');
		}
		
		return sb.toString();
	}

	@Override
	Object access(Object previous) throws AccessingFailureException {
		try {
			this.method.setAccessible(true);
			if(this.mode == Mode.SIMPLE) {
				try {
					return this.method.invoke(previous, previous);
				} catch (IllegalArgumentException e) {
					String argList = "[" + previous == null ? "null" : previous.getClass().getCanonicalName() + "]";
					throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
							argList, this.method.toString());
				}
			} else if(this.mode == Mode.NORMAL) {
				Object[] argObjs = new Object[this.arguments.length];
				Type[] argTypes = this.method.getGenericParameterTypes();
				for(int i = 0; i < this.arguments.length; i++) {
					Literal<?> l = this.arguments[i];
					if(l != null) {
						try {
							argObjs[i] = l.get(argTypes[i]);	// XXX Generic type
						} catch (InvalidLiteralException e) {
							throw AccessingFailureException.create(e, this);
						}
					} else {
						argObjs[i] = previous;
					}
				}
				
				try {
					return this.method.invoke(previous, argObjs);
				} catch (IllegalArgumentException e) {
					throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
							Arrays.toString(argObjs), this.method.toString());
				}
			} else {
				throw new IllegalStateException();
			}
		} catch (InvocationTargetException e) {
			throw AccessingFailureException.createWithArgs(FailureCause.INVOKE_FAIL, this, e.getCause(), 
					this.method.getName(), e.getCause());
		} catch (AccessingFailureException e) {
			throw e;
		} catch (Exception e) {
			throw AccessingFailureException.createWithArgs(FailureCause.ERROR, this, e, e);
		}
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		return this.method.getGenericReturnType();
	}
	
	@Override
	public boolean allowsPrimitiveTypes() {
		return this.method != null && Modifier.isStatic(this.method.getModifiers());
	}

	private enum Mode {
		SIMPLE, 
		NORMAL
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			InsnList insns = new InsnList();
			if(this.method == null) {
				throw new CompilationException(FailureCause.ERROR, (Object) null);
			} else {
				// 1. Prepare arguments
				if(!Modifier.isStatic(this.method.getModifiers())) {
					insns.add(new InsnNode(Opcodes.DUP));
				}
				
				int lastOutIdx = BytecodeHelper.appendLocalVarStorer(ctx, insns, ctx.getLastOutputClass());
				int argsLength = this.arguments.length;
				Class<?>[] argTypes = this.method.getParameterTypes();
				for(int i = 0; i < argsLength; i++) {
					Literal<?> literal = this.arguments[i];
					if(literal == null) {
						BytecodeHelper.appendLocalVarLoader(insns, lastOutIdx, ctx.getLastOutputClass());
						if(argTypes[i].isPrimitive() && !ctx.getLastOutputClass().isPrimitive()) {
							BytecodeHelper.appendPrimitiveUnwrapper(insns, argTypes[i]);
						}
						
						if(!argTypes[i].isPrimitive() && ctx.getLastOutputClass().isPrimitive()) {
							BytecodeHelper.appendPrimitiveWrapper(insns, argTypes[i]);
						}
					} else {
						BytecodeHelper.appendConstantLoader(ctx, insns, literal, argTypes[i]);
					}
				}
				
				// 2. Invoke underlying method
				BytecodeHelper.appendCaller(insns, this.method, CompilationContext.CallableType.INVOKER);
			}
			
			ctx.endNode(this.method.getReturnType());
			return insns;
		};
	}
}
