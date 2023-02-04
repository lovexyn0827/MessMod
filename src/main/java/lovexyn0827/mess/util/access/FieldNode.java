package lovexyn0827.mess.util.access;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
	protected Type prepare(Type lastOutType) throws AccessingFailureException {
		Field f;
		if(lastOutType instanceof Class<?>) {
			f = Reflection.getFieldFromNamed((Class<?>) lastOutType, this.fieldName);
		} else if(lastOutType instanceof ParameterizedType) {
			f = Reflection.getFieldFromNamed((Class<?>) ((ParameterizedType) lastOutType).getRawType(), this.fieldName);
		} else {
			f = null;
		}
		
		if(f != null) {
			this.field = f;
			this.outputType = f.getGenericType();
			return this.outputType;
		} else {
			throw AccessingFailureException.createWithArgs(FailureCause.NO_FIELD, this, null, 
					this.fieldName, lastOutType.getTypeName());
		}
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
			this.field.set(writeTo, newValue);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw AccessingFailureException.createWithArgs(FailureCause.BAD_ARG, this, e, 
					newValue == null ? "null" : newValue, this.fieldName);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw AccessingFailureException.create(FailureCause.ERROR, this, e);
		}
	}
}
