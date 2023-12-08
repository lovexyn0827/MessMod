package lovexyn0827.mess.util.access;

import org.objectweb.asm.tree.InsnList;

interface NodeCompiler {
	/**
	 * Generate a sequence of bytecode performing the operation of a node.</br>
	 * The element at the top of the operand stack is the output of the last node and should be the output of this node
	 *  after execution.
	 * Using variables is not permitted.
	 * @param ctx Context
	 * @return
	 */
	InsnList compile(CompilationContext ctx) throws CompilationException;
}
