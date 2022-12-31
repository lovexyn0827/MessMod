package lovexyn0827.mess.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

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
	private final Literal<?>[] arguments;
	private final Mode mode;
	
	public MapperNode(String toParse) throws CommandSyntaxException {
		//Class::method<...>(...)
		Matcher matcher = PATTERN.matcher(toParse);
		if(matcher.matches()) {
			String name = matcher.group("name");
			String className = matcher.group("class").replace('/', '.');
			if(name == null && className == null) {
				throw new TranslatableException("exp.mapper.format");
			}
			
			Mutable<String> srg = new MutableObject<>();
			Mapping map = MessMod.INSTANCE.getMapping();
			String typesStr = matcher.group("types");
			String argsStr = matcher.group("args");
			try {
				Optional<Method> mayMethod;
				Class<?> clazz = Class.forName(className);
				if(typesStr == null) {
					// Class::method, only methods with a single parameter and returns something.
					mayMethod = Reflection.listMethods(clazz).stream()
							.filter((m) -> {
								String descriptor = org.objectweb.asm.Type.getMethodDescriptor(m);
								srg.setValue(map.srgMethodRecursively(m.getDeclaringClass(), name, descriptor));
								return m.getName().equals(srg.getValue()) && m.getParameterCount() == 1 
										&& !m.isSynthetic() && m.getReturnType() != Void.TYPE;
							})
							.findFirst();
					this.arguments = new Literal<?>[] { null };
					this.mode = Mode.SIMPLE;
				} else if(typesStr != null && argsStr != null) {
					// Class::method<types>(args)
					if(typesStr.matches("[0-9]+")) {
						// The number of parameters is given
						int argNum = Integer.parseInt(typesStr);
						List<Method> targets = Reflection.listMethods(clazz).stream()
								.filter((m) -> m.getParameterCount() == argNum)
								.collect(Collectors.toList());
						if(targets.size() > 1) {
							throw new TranslatableException("exp.multitarget");
						} else if (targets.size() == 0) {
							mayMethod = Optional.empty();
						} else {
							mayMethod = Optional.ofNullable(targets.get(0));
						}
					} else {
						// The descriptor is given
						// TODO Handle overriding / overridden methods better.
						Class<?>[] types = MethodNode.parseDescriptor(typesStr);
						mayMethod = Reflection.listMethods(clazz).stream()
								.filter((m) -> {
									String descriptor = org.objectweb.asm.Type.getMethodDescriptor(m);
									srg.setValue(map.srgMethodRecursively(m.getDeclaringClass(), name, descriptor));
									return m.getName().equals(srg.getValue()) && Arrays.equals(types, m.getParameterTypes()) 
											&& !m.isSynthetic();
								})
								.findFirst();
					}
					
					String[] argLS = argsStr.split(",");
					Literal<?>[] argL = new Literal<?>[argLS.length];
					for(int i = 0; i < argLS.length; i++) {
						String str = argLS[i];
						if("_".equals(str)) {
							argL[i] = null;
							continue;
						} else {
							argL[i] = Literal.parse(str);
						}
					}
					
					this.arguments = argL;
					this.mode = Mode.NORMAL;
				} else {
					throw new TranslatableException("exp.mapper.format");
				}
				
				if(mayMethod.isPresent()) {
					this.method = mayMethod.get();
				} else {
					String mS = name + ((typesStr == null) ? "" : '<' + typesStr + ">");
					throw new TranslatableException("exp.nomethod", 
							mS, clazz.getCanonicalName());
				}
			} catch (ClassNotFoundException e) {
				TranslatableException e1 = new TranslatableException("exp.noclass", className);
				e1.initCause(e);
				throw e1;
			}
		} else {
			throw new TranslatableException("exp.mapper.format");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		result = prime * result + Objects.hash(method, mode);
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
		return Arrays.equals(arguments, other.arguments) && Objects.equals(method, other.method) && mode == other.mode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.method.getDeclaringClass().getCanonicalName().replace('/', '/'));
		sb.append("::");
		sb.append(this.method.getName());
		if(this.mode != Mode.SIMPLE) {
			sb.append('<');
			sb.append(org.objectweb.asm.Type.getMethodDescriptor(this.method));
			sb.append('>');
			sb.append('(');
			for(Literal<?> l : this.arguments) {
				sb.append(l.stringRepresentation);
				sb.append(',');
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
					throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
							"[]", this.method.toString());
				}
			} else if(this.mode == Mode.NORMAL) {
				try {
					Object[] argObjs = new Object[this.arguments.length];
					for(int i = 0; i < this.arguments.length; i++) {
						Literal<?> l = this.arguments[i];
						if(l != null) {
							argObjs[i] = l.get(this.inputType);	// XXX Generic type
						} else {
							argObjs[i] = previous;
						}
					}
					
					return this.method.invoke(previous, argObjs);
				} catch (IllegalArgumentException e) {
					throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
							"[]", this.method.toString());
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
			e.printStackTrace();
			MessMod.LOGGER.info("Failed to invoke " + this.method);
			throw AccessingFailureException.createWithArgs(FailureCause.ERROR, this, e, e);
		}
	}

	@Override
	protected Type prepare(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		return this.method.getGenericReturnType();
	}

	private enum Mode {
		SIMPLE, 
		NORMAL
	}
}
