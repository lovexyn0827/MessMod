package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.TranslatableException;

public class ClassCastNode extends Node {
	private final Class<?> castTo;
	
	public ClassCastNode(String className) {
		try {
			String runtimtClassName = MessMod.INSTANCE.getMapping().srgClass(className.replace('/', '.'));
			this.castTo = Class.forName(runtimtClassName);
		} catch (ClassNotFoundException e) {
			TranslatableException e1 = new TranslatableException("exp.noclass", className);
			e1.initCause(e);
			throw e1;
		}
	}

	@Override
	Object access(Object previous) throws AccessingFailureException {
		try {
			return this.castTo.cast(previous);
		} catch (ClassCastException e) {
			throw AccessingFailureException.createWithArgs(FailureCause.CAST, this, e, 
					previous.getClass().getCanonicalName(), this.castTo.getCanonicalName());
		}
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		return this.castTo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.castTo);
	}

	@Override
	public String toString() {
		return '(' + this.castTo.getCanonicalName().replace('.', '/') + ')';
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
		
		ClassCastNode other = (ClassCastNode) obj;
		return Objects.equals(this.castTo, other.castTo);
	}

	@Override
	NodeCompiler getCompiler() {
		this.ensureInitialized();
		return (ctx) -> {
			InsnList insns = new InsnList();
			insns.add(new TypeInsnNode(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(this.castTo)));
			ctx.endNode(this.castTo);
			return insns;
		};
	}

	
}
