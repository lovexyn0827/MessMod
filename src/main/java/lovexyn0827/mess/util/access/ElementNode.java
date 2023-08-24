package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableCollection;
import lovexyn0827.mess.util.Reflection;

final class ElementNode extends Node {
	private int index;

	ElementNode(int index) {
		this.index = index;
	}
	
	@Override
	Object access(Object previous) throws AccessingFailureException {
		if(previous.getClass().isArray()) {
			try {
				if(this.index >= 0) {
					return Array.get(previous, this.index);
				} else {
					return Array.get(previous, Array.getLength(previous) + this.index);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw AccessingFailureException.create(FailureCause.OUT_OF_BOUND, this);
			}
		} else if(previous instanceof Collection<?>) {
			Collection<?> c = (Collection<?>) previous;
			if(this.index < c.size()) {
				Iterator<?> itr = c.iterator();
				for(int i = 0; i < this.index; i++) {
					itr.next();
				}
				
				return itr.next();
			} else {
				throw AccessingFailureException.create(FailureCause.OUT_OF_BOUND, this);
			}
		} else {
			throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
		}
	}
	
	@Override
	public int hashCode() {
		return this.index ^ (this.outputType != null ? this.outputType.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || ElementNode.class != obj.getClass()) {
			return false;
		}
		
		ElementNode other = (ElementNode) obj;
		return this.index == other.index
				&& (this.outputType == null ? other.outputType == null : this.outputType.equals(other.outputType));
	}
	
	@Override
	public String toString() {
		return '[' + Integer.toString(this.index) + ']';
	}
	
	@Override
	boolean canFollow(Node n) {
		Type t = n.outputType;
		return t instanceof Class<?> && (((Class<?>) t).isArray() || Collection.class.isAssignableFrom((Class<?>) t));
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) {
		if(lastOutType instanceof ParameterizedType) {
			Type typeArg = ((ParameterizedType) lastOutType).getActualTypeArguments()[0];
			return typeArg;
		} else if(lastOutType instanceof Class<?>) {
			Class<?> cl = (Class<?>) lastOutType;
			if(cl.isArray()) {
				Class<?> compType = cl.getComponentType();
				return compType;
			}
		}
		
		return Object.class;
	}

	@Override
	Node createCopyForInput(Object input) {
		ElementNode node = new ElementNode(this.index);
		node.ordinary = this.ordinary;
		return node;
	}

	@Override
	boolean isWrittable() {
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	void write(Object writeTo, Object newValue) throws AccessingFailureException {
		if(ImmutableCollection.class.isAssignableFrom(Reflection.getRawType(inputType))) {
			throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
		} else {
			if(writeTo.getClass().isArray()) {
				try {
					if(this.index >= 0) {
						Array.set(writeTo, this.index, newValue);
					} else {
						Array.set(writeTo, Array.getLength(writeTo) + this.index, newValue);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					throw AccessingFailureException.create(FailureCause.OUT_OF_BOUND, this);
				}
			} else if(writeTo instanceof Collection<?>) {
				if(writeTo instanceof List<?>) {
					if(newValue == null
							|| Reflection.getRawType(this.outputType).isAssignableFrom(newValue.getClass())) {
						try {
							((List) writeTo).add(this.index, newValue);
						} catch (IndexOutOfBoundsException e) {
							throw AccessingFailureException.create(FailureCause.OUT_OF_BOUND, this);
						} catch (UnsupportedOperationException e) {
							throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
						}
					} else {
						throw AccessingFailureException.createWithArgs(
								FailureCause.INV_LAST_W, this, null, this, newValue);
					}
				} else {
					throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
				}
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
			}
		}
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			Type inType = ctx.getLastOutputType();
			InsnList insns = new InsnList();
			Class<?> rawType = Reflection.getRawType(inType);
			Class<?> out;
			if(rawType.isArray()) {
				/* (Load index)
				 * ~ALOAD
				 * (Wrap)?
				 */
				BytecodeHelper.appendArrayElementLoader(insns, this.index, rawType.getComponentType());
				out = rawType.getComponentType();
			} else if(Collection.class.isAssignableFrom(rawType)) {
				/* checkcast List / Collection
				 * dup
				 * invoke size()I
				 * (Load index)
				 * dup_x1	// List, index, size, index
				 * if_icmpgt l1	//if index > size
				 * (throw)
				 * l1:
				 * 	get stuff
				 */
				boolean isList = List.class.isAssignableFrom(rawType);
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, isList ? "java/util/List" : "java/util/Collection"));
				insns.add(new InsnNode(Opcodes.DUP));
				insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Collection", 
						"size", "()I"));
				BytecodeHelper.appendIntegerLoader(insns, this.index);
				if(isList) {
					insns.add(new InsnNode(Opcodes.DUP_X1));
				}
				
				LabelNode ifInBound = new LabelNode();
				insns.add(new JumpInsnNode(Opcodes.IF_ICMPGT, ifInBound));
				BytecodeHelper.appendAccessingFailureException(insns, FailureCause.OUT_OF_BOUND);
				insns.add(ifInBound);
				// List, int / Collection
				if(isList) {
					insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", 
							"get", "(I)Ljava/lang/Object;"));
				} else {
					insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Collection", 
							"iterator", "()Ljava/util/Iterator;"));
					if(this.index >= 0 && this.index <= -1) {
						// Possibly better solution for smaller indexes
						for(int i = 0; i < this.index; i++) {
							insns.add(new InsnNode(Opcodes.DUP));
							insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", 
									"next", "()Ljava/lang/Object;"));
							insns.add(new InsnNode(Opcodes.POP));
						}
						
						insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", 
								"next", "()Ljava/lang/Object;"));
					} else {
						/* (load index)
						 * istore var
						 * aconst_null
						 * loop:
						 * 	pop
						 * 	dup
						 * 	invoke next()
						 * 	iinc var
						 * 	iload var
						 * 	ifge loop // jump if var >= 0
						 * invoke next()
						 */
						int var = ctx.allocateLocalVar();
						BytecodeHelper.appendIntegerLoader(insns, this.index);
						insns.add(new VarInsnNode(Opcodes.ISTORE, var));
						insns.add(new InsnNode(Opcodes.ACONST_NULL));
						LabelNode beginLoop = new LabelNode();
						// ...,Iterator, Object
						insns.add(beginLoop);
						insns.add(new InsnNode(Opcodes.POP));
						insns.add(new InsnNode(Opcodes.DUP));
						// ..., Iterator, Iterator
						insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Iterator", 
								"next", "()Ljava/lang/Object;"));
						// ..., Iterator, Object
						insns.add(new IincInsnNode(var, -1));
						insns.add(new VarInsnNode(Opcodes.ILOAD, var));
						// ..., Iterator, Object, int
						insns.add(new JumpInsnNode(Opcodes.IFGE, beginLoop));
					}
				}
				
				out = Reflection.getTypeArgOrObject(inType, 0);
			} else {
				throw new CompilationException(FailureCause.INV_LAST, this);
			}
			
			ctx.endNode(out);
			return insns;
		};
	}
}
