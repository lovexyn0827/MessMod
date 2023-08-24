package lovexyn0827.mess.util.access;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.objectweb.asm.tree.InsnList;
import lovexyn0827.mess.util.Reflection;

final class FieldNode extends Node {
	private final String fieldName;
	private Field field;
	
	FieldNode(String fn) {
		this.fieldName = fn;
	}
	
	@Override
	Object access(Object previous) throws AccessingFailureException {
		try {
			this.field.setAccessible(true);
			return this.field.get(previous);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw AccessingFailureException.createWithArgs(FailureCause.NO_FIELD, this, e, 
					this.fieldName, previous.getClass().getSimpleName());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw AccessingFailureException.create(FailureCause.ERROR, this, e);
		}
	}
	
	@Override
	public int hashCode() {
		return this.fieldName.hashCode() ^ (this.outputType != null ? this.outputType.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || FieldNode.class != obj.getClass()) {
			return false;
		}
		
		FieldNode other = (FieldNode) obj;
		return (this.field == null ? this.fieldName.equals(other.fieldName) : this.field.equals(other.field))
				&& (this.outputType == null ? other.outputType == null : this.outputType.equals(other.outputType));
	}
	
	@Override
	public String toString() {
		return '!' + this.fieldName;
	}
	
	@Override
	boolean canFollow(Node n) {
		return n.outputType != null && Reflection.hasField(
				Reflection.getRawType(n.outputType), this.fieldName);
	}

	@Override
	void uninitialize() {
		super.uninitialize();
		this.field = null;
	}
	
	@Override
	void initialize(Type lastOutType) throws AccessingFailureException {
		this.field = this.resolveField(lastOutType);
		if(this.field == null) {
			throw AccessingFailureException.createWithArgs(FailureCause.NO_FIELD, this, null, 
					this.fieldName, lastOutType.getTypeName());
		}
		
		super.initialize(lastOutType);
	}
	
	private Field resolveField(Type lastOutType) throws AccessingFailureException {
		return Reflection.getFieldFromNamed(Reflection.getRawType(lastOutType), this.fieldName);
	}

	@Override
	protected Type resolveOutputType(Type lastOutType) throws AccessingFailureException {
		return this.field.getGenericType();
	}

	@Override
	Node createCopyForInput(Object input) {
		FieldNode node = new FieldNode(this.fieldName);
		node.ordinary = this.ordinary;
		return node;
	}
	
	@Override
	boolean isWrittable() {
		return (this.field != null) && !Modifier.isFinal(this.field.getModifiers());
	}
	
	@Override
	void write(Object writeTo, Object newValue) throws AccessingFailureException {
		try {
			this.field.setAccessible(true);
			this.field.set(writeTo, newValue);
		} catch (IllegalArgumentException e) {
			throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
					newValue == null ? "null" : newValue, this.fieldName);
		} catch (IllegalAccessException e) {
			throw AccessingFailureException.create(FailureCause.ERROR, this, e);
		}
	}

	@Override
	NodeCompiler getCompiler() {
		return (ctx) -> {
			InsnList insns = new InsnList();
			if(this.field == null) {
				throw new CompilationException(FailureCause.ERROR, (Object) null);
			} else {
				BytecodeHelper.appendCaller(insns, this.field, CompilationContext.CallableType.GETTER);
			}
			
			ctx.endNode(this.field.getGenericType());
			return insns;
		};
	}
}
