package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class SizeNode extends Node {
	@Override
	boolean canFollow(Node n) {
		Type t = n.outputType;
		return t instanceof Class<?> && (((Class<?>) t).isArray() || Collection.class.isAssignableFrom((Class<?>) t));
	}
	
	@Override
	Object access(Object previous) throws AccessingFailureException {
		if(previous.getClass().isArray()) {
			return Array.getLength(previous);
		} else if(previous instanceof Collection<?>) {
			Collection<?> c = (Collection<?>) previous;
			return c.size();
		} else if(previous instanceof CharSequence) {
			return ((CharSequence) previous).length();
		} else if(previous instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) previous;
			return m.size();
		} else {
			throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
		}
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) {
		return int.class;
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			InsnList insns = new InsnList();
			Class<?> clazz = ctx.getLastOutputClass();
			if(clazz.isArray()) {
				insns.add(new InsnNode(Opcodes.ARRAYLENGTH));
			} else if(Collection.class.isAssignableFrom(clazz)) {
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/util/Collection"));
				insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Collection", 
						"size", "()I"));
			} else if(CharSequence.class.isAssignableFrom(clazz)) {
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/CharSequence"));
				insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/lang/CharSequence", 
						"length", "()I"));
			} else if(Map.class.isAssignableFrom(clazz)) {
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/util/Map"));
				insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", 
						"size", "()I"));
			} else {
				throw new CompilationException(FailureCause.INV_LAST, this);
			}
			
			ctx.endNode(int.class);
			return insns;
		};
	}

}
