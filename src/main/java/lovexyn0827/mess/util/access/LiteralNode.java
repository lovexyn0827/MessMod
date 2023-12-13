package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.util.ArgumentListTokenizer;
import lovexyn0827.mess.util.Reflection;

public class LiteralNode extends Node {
	private Literal<?> literal;

	public LiteralNode(String nodeStr) throws CommandSyntaxException {
		this.literal = Literal.parse(new ArgumentListTokenizer(nodeStr).next());
	}

	@Override
	Object access(Object previous) throws AccessingFailureException {
		try {
			return this.literal.get(this.outputType);
		} catch (InvalidLiteralException e) {
			throw AccessingFailureException.create(e, this);
		}
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		Object ob = this.literal.get(null);
		return ob == null ? Object.class : ob.getClass();
	}
	
	@Override
	void initialize(Type lastOutType) throws AccessingFailureException {
		try {
			this.literal.get(null);
		} catch (InvalidLiteralException e) {
			throw AccessingFailureException.create(e, this);
		}
		
		super.initialize(lastOutType);
	}
	
	@Override
	void uninitialize() {
		this.literal = this.literal.recreate();
	}
	
	@Override
	boolean allowsPrimitiveTypes() {
		return true;
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			InsnList insns = new InsnList();
			Type lastOut = ctx.getLastOutputType();
			insns.add(new InsnNode(lastOut == double.class || lastOut == long.class ? Opcodes.POP2 : Opcodes.POP));
			BytecodeHelper.appendConstantLoader(ctx, insns, this.literal, 
					Reflection.getRawType(this.outputType));
			ctx.endNode(this.outputType);
			return insns;
		};
	}

}
