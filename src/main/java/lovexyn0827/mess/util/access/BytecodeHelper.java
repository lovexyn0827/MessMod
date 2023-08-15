package lovexyn0827.mess.util.access;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

final class BytecodeHelper {
	static final ImmutableSet<Class<?>> VALID_TYPES_IN_LDC = ImmutableSet.<Class<?>>builder()
			.add(Integer.class)
			.add(Double.class)
			.add(Float.class)
			.add(Long.class)
			.add(String.class)
			.build();
	static final String UNCERTAIN_FIELD_DESCRIPTOR = null;
	
	private BytecodeHelper() {}

	static void appendPrimitiveWrapper(InsnList insns, Class<?> clazz) {
		// Disgusting, but efficient
		if(clazz == int.class || clazz == Integer.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Integer.class), 
					"valueOf", "(I)Ljava/lang/Integer;"));
		} else if(clazz == long.class || clazz == long.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Long.class), 
					"valueOf", "(J)Ljava/lang/Long;"));
		} else if(clazz == float.class || clazz == Float.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Float.class), 
					"valueOf", "(F)Ljava/lang/Float;"));
		} else if(clazz == double.class || clazz == Double.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Double.class), 
					"valueOf", "(D)Ljava/lang/Double;"));
		} else if(clazz == boolean.class || clazz == Boolean.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Boolean.class), 
					"valueOf", "(Z)Ljava/lang/Boolean;"));
		} else if(clazz == char.class || clazz == Character.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Character.class), 
					"valueOf", "(C)Ljava/lang/Character;"));
		} else if(clazz == byte.class || clazz == Byte.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Byte.class), 
					"valueOf", "(B)Ljava/lang/Byte;"));
		} else if(clazz == short.class || clazz == Short.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					org.objectweb.asm.Type.getInternalName(Short.class), 
					"valueOf", "(S)Ljava/lang/Short;"));
		} else {
			throw new IllegalArgumentException("Illegal input class: " + clazz.getCanonicalName());
		}
	}
	
	static void appendPrimitiveUnwrapper(InsnList insns, Class<?> clazz) {
		// Disgusting, but efficient
		if(clazz == Integer.class || clazz == int.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Integer.class), 
					"intValue", "()I"));
		} else if(clazz == Long.class || clazz == long.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Long.class), 
					"longValue", "()J"));
		} else if(clazz == Float.class || clazz == float.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Float.class), 
					"floatValue", "()F"));
		} else if(clazz == Double.class || clazz == double.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Double.class), 
					"doubleValue", "()D"));
		} else if(clazz == Boolean.class || clazz == boolean.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Boolean.class), 
					"booleanValue", "()Z"));
		} else if(clazz == Character.class || clazz == char.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Character.class), 
					"charValue", "()C"));
		} else if(clazz == Byte.class || clazz == byte.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Byte.class), 
					"byteValue", "()B"));
		} else if(clazz == Short.class || clazz == short.class) {
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
					org.objectweb.asm.Type.getInternalName(Short.class), 
					"shortValue", "()S"));
		} else {
			throw new IllegalArgumentException();
		}
	}

	static void appendIntegerLoader(InsnList insns, int integer) {
		if(integer <= 5) {
			insns.add(new InsnNode(Opcodes.ICONST_0 + integer));
		} else if(integer <= 255) {
			insns.add(new IntInsnNode(Opcodes.BIPUSH, integer));
		} else if(integer <= 65535) {
			insns.add(new IntInsnNode(Opcodes.SIPUSH, integer));
		} else {
			insns.add(new LdcInsnNode(integer));
		}
	}

	static void appendConstantLoader(CompilationContext ctx, InsnList insns, Literal<?> literal, Class<?> type) 
			throws CompilationException  {
		if(literal.isStatic()) {
			Object obj;
			try {
				obj = literal.get(null);
			} catch (InvalidLiteralException e) {
				throw new CompilationException(e.failureCause, e.args);
			}
			
			bl1: 
			if(VALID_TYPES_IN_LDC.contains(obj.getClass())) {
				// If supported in LDC
				if(obj instanceof Byte) {
					insns.insert(new LdcInsnNode(((Byte) obj).intValue()));
					insns.add(new InsnNode(Opcodes.I2B));
				} else if(obj instanceof Short) {
					insns.add(new LdcInsnNode(((Short) obj).intValue()));
					insns.add(new InsnNode(Opcodes.I2S));
				} else if(obj instanceof Character) {
					insns.add(new LdcInsnNode(((Character) obj).charValue()));
					insns.add(new InsnNode(Opcodes.I2C));
				} else if(obj instanceof Boolean) {
					insns.add(new LdcInsnNode(((Boolean) obj) ? 1 : 0));
				} else if(obj instanceof String) {
					insns.add(new LdcInsnNode(obj));
					break bl1;
				} else if(obj instanceof Class) {
					insns.add(new LdcInsnNode(org.objectweb.asm.Type.getType((Class<?>) obj)));
					break bl1;
				} else {
					insns.add(new LdcInsnNode(obj));
				}
				
				if(!type.isPrimitive()) {
					appendPrimitiveWrapper(insns, type);
				}
			} else {
				Pair<Integer, Integer> id = ctx.allocateStaticLiteral(literal);
				appendReferenceConstantGetter(ctx, insns, "SC$" + id.getFirst(), id.getSecond());
				if(type.isPrimitive()) {
					appendPrimitiveUnwrapper(insns, type);
				}
			}
		} else {
			int id = ctx.allocateDynamicLiteral(literal);
			appendReferenceConstantGetter(ctx, insns, "DYNAMIC_LITERALS", id);
			
		}
	}
	
	static void appendReferenceConstantGetter(CompilationContext ctx, InsnList insns, String fieldName, int cid) {
		insns.add(new FieldInsnNode(Opcodes.GETSTATIC, ctx.getInternalClassNameOfPath(), 
				fieldName, UNCERTAIN_FIELD_DESCRIPTOR));
		appendIntegerLoader(insns, cid);
		insns.add(new InsnNode(Opcodes.AALOAD));
	}

	static void appendAccessingFailureException(InsnList insns, FailureCause cause) {
		insns.add(new FieldInsnNode(Opcodes.GETSTATIC, "lovexyn0827/mess/util/access/FailureCause", 
				cause.name(), "Llovexyn0827/mess/util/access/FailureCause;"));
		insns.add(new InsnNode(Opcodes.ACONST_NULL));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
				"lovexyn0827/mess/util/access/AccessingFailureException", "create", 
				"(Llovexyn0827/mess/util/access/FailureCause;Llovexyn0827/mess/util/access/Node;)"
				+ "Llovexyn0827/mess/util/access/AccessingFailureException;"));
		insns.add(new InsnNode(Opcodes.ATHROW));
	}

	/**
	 * Note that only caller instructions like invokevirtual or getfield will be appended, 
	 * and argument preparation won't be done done.
	 */
	static void appendCaller(InsnList insns, Member mem, CompilationContext.CallableType callableType) {
		if(mem != null) {
			boolean isStatic = Modifier.isStatic(mem.getModifiers());
			int opcode;
			String desc;
			switch(callableType) {
			case INVOKER: 
				Method m = (Method) mem;
				desc = org.objectweb.asm.Type.getMethodDescriptor(m);
				String operationDesc;
				if(!Modifier.isStatic(m.getModifiers())) {
					operationDesc = desc.replace("(", "(" + 
							org.objectweb.asm.Type.getDescriptor(m.getDeclaringClass()));
				} else {
					operationDesc = desc;
				}
				
				if(canAccessDirectly(mem)) {
					if(isStatic) {
						opcode = Opcodes.INVOKESTATIC;
					} else if(m.getDeclaringClass().isInterface()) {
						opcode = Opcodes.INVOKEINTERFACE;
					} else if(Modifier.isPrivate(m.getModifiers())) {
						opcode = Opcodes.INVOKESPECIAL;
					} else {
						opcode = Opcodes.INVOKEVIRTUAL;
					}
					
					insns.add(new MethodInsnNode(opcode, 
							org.objectweb.asm.Type.getInternalName(m.getDeclaringClass()), 
							m.getName(), desc));
				} else {
					insns.add(new InvokeDynamicInsnNode("inDy_" + m.getName(), operationDesc, 
							CompiledPath.METHOD_BSM_HANDLE, 
							org.objectweb.asm.Type.getInternalName(m.getDeclaringClass()), m.getName(), 
							desc));
				}
				
				break;
			case GETTER: 
				Field f = (Field) mem;
				if(canAccessDirectly(mem)) {
					opcode = isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
					insns.add(new FieldInsnNode(opcode, org.objectweb.asm.Type.getInternalName(f.getDeclaringClass()), 
							f.getName(), org.objectweb.asm.Type.getDescriptor(f.getType())));
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append('(');
					if(!isStatic) {
						sb.append(org.objectweb.asm.Type.getDescriptor(f.getDeclaringClass()));
					}
					
					sb.append(')');
					sb.append(org.objectweb.asm.Type.getDescriptor(f.getType()));
					desc = sb.toString();
					insns.add(new InvokeDynamicInsnNode("inDy_" + f.getName(), desc, CompiledPath.FIELD_BSM_HANDLE, 
							org.objectweb.asm.Type.getInternalName(f.getDeclaringClass()), f.getName(), 
							CompiledPath.IN_DY_GETTER));
				}
				
				break;
			case SETTER: 
				Field f1 = (Field) mem;
				if(canAccessDirectly(mem)) {
					opcode = isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
					insns.add(new FieldInsnNode(opcode, 
							org.objectweb.asm.Type.getInternalName(f1.getDeclaringClass()), 
							f1.getName(), org.objectweb.asm.Type.getDescriptor(f1.getType())));
				} else {
					StringBuilder sb1 = new StringBuilder();
					sb1.append('(');
					if(!isStatic) {
						sb1.append(org.objectweb.asm.Type.getDescriptor(f1.getDeclaringClass()));
					}
					
					sb1.append(org.objectweb.asm.Type.getDescriptor(f1.getType()));
					sb1.append(")V");
					desc = sb1.toString();
					insns.add(new InvokeDynamicInsnNode("inDy_" + f1.getName(), desc, CompiledPath.FIELD_BSM_HANDLE, 
							org.objectweb.asm.Type.getInternalName(f1.getDeclaringClass()), f1.getName(), 
							CompiledPath.IN_DY_SETTER));
				}
				
				break;
			}
		}
	}

	private static boolean canAccessDirectly(Member mem) {
		return Modifier.isPublic(mem.getModifiers()) && 
				Modifier.isPublic(mem.getDeclaringClass().getModifiers());
	}
	
	/**
	 * @return The slot in which the variable is stored.
	 */
	static int appendLocalVarStorer(CompilationContext ctx, InsnList insns, Class<?> type) {
		if(type.isPrimitive()) {
			int slot;
			if(type == float.class) {
				slot = ctx.allocateLocalVar();
				insns.add(new VarInsnNode(Opcodes.FSTORE, slot));
			} else if(type == long.class) {
				slot = ctx.allocateWideLocalVar();
				insns.add(new VarInsnNode(Opcodes.LSTORE, slot));
			} else if(type == double.class) {
				slot = ctx.allocateWideLocalVar();
				insns.add(new VarInsnNode(Opcodes.DSTORE, slot));
			} else {
				slot = ctx.allocateLocalVar();
				insns.add(new VarInsnNode(Opcodes.ISTORE, slot));
			}
			
			return slot;
		} else {
			int slot = ctx.allocateLocalVar();
			insns.add(new VarInsnNode(Opcodes.ASTORE, slot));
			return slot;
		}
	}
	
	static void appendLocalVarLoader(InsnList insns, int slot, Class<?> type) {
		if(type.isPrimitive()) {
			if(type == float.class) {
				insns.add(new VarInsnNode(Opcodes.FLOAD, slot));
			} else if(type == long.class) {
				insns.add(new VarInsnNode(Opcodes.LLOAD, slot));
			} else if(type == double.class) {
				insns.add(new VarInsnNode(Opcodes.DLOAD, slot));
			} else {
				insns.add(new VarInsnNode(Opcodes.ILOAD, slot));
			}
		} else {
			insns.add(new VarInsnNode(Opcodes.ALOAD, slot));
		}
	}

	public static void appendArrayElementLoader(InsnList insns, int index, Class<?> type) {
		appendIntegerLoader(insns, index);
		switch(type.getName()) {
		case "int" :
			insns.add(new InsnNode(Opcodes.IALOAD));
			break;
		case "double" :
			insns.add(new InsnNode(Opcodes.DALOAD));
			break;
		case "float" :
			insns.add(new InsnNode(Opcodes.FALOAD));
			break;
		case "long" :
			insns.add(new InsnNode(Opcodes.LALOAD));
			break;
		case "short" :
			insns.add(new InsnNode(Opcodes.SALOAD));
			break;
		case "char" :
			insns.add(new InsnNode(Opcodes.CALOAD));
			break;
		case "boolean" :
		case "byte" :
			insns.add(new InsnNode(Opcodes.BALOAD));
			break;
		default :
			insns.add(new InsnNode(Opcodes.AALOAD));
		}
	}
}
