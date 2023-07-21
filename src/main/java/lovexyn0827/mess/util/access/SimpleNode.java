package lovexyn0827.mess.util.access;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.function.Function;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import org.objectweb.asm.Opcodes;
import lovexyn0827.mess.util.Reflection;

class SimpleNode<I, O> extends Node {
	private static final SerializableFunction<?, ?>[] ALL_FUNCTIONS = new SerializableFunction<?, ?>[64];
	private static int functionCount;
	static final SimpleNode<Object, Integer> IDENTITY_HASH = 
			new SimpleNode<>(System::identityHashCode, "identityHash", Object.class, Integer.class, false);
	private final SerializableFunction<I, O> func;
	private final String name;
	private final Class<?> in;
	private final Class<?> out;
	private final boolean allowsPrimitiveTypes;
	private final int funcId;

	// This constructor is set to private to make equals() work properly by avoiding duplication
	private SimpleNode(SerializableFunction<I, O> func, String name, Class<? extends I> in, Class<? super O> out, 
			boolean allowsPrimitiveTypes) {
		this.func = func;
		this.name = name;
		this.in = in;
		this.out = out;
		this.allowsPrimitiveTypes = allowsPrimitiveTypes;
		this.funcId = functionCount++;
		ALL_FUNCTIONS[this.funcId] = func;
	}
	
	@SuppressWarnings("unchecked")	// XXX
	@Override
	Object access(Object previous) {
		return func.apply((I) previous);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	boolean canFollow(Node n) {
		Class<?> last = Reflection.getRawType(n.outputType);
		return last == null || this.in.isAssignableFrom(last);
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) {
		return this.out;
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			InsnList insns = new InsnList();
			insns.add(new FieldInsnNode(Opcodes.GETSTATIC, ctx.getInternalClassNameOfPath(), 
					"LAMBDAS", "[Ljava/util/function/Function;"));
			BytecodeHelper.appendIntegerLoader(insns, ctx.allocateLambda(this.func));
			insns.add(new InsnNode(Opcodes.AALOAD));
			insns.add(new InsnNode(Opcodes.SWAP));
			insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/function/Function", 
					"apply", "(Ljava/lang/Object;)Ljava/lang/Object;"));
			return insns;
		};
	}
	
	@Override
	boolean allowsPrimitiveTypes() {
		return this.allowsPrimitiveTypes;
	}
	
	private interface SerializableFunction<I, O> extends Serializable, Function<I, O> {
	}
}
