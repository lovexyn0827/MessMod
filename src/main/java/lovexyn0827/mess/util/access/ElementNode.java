package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
	protected Type prepare(Type lastOutType) {
		if(lastOutType instanceof ParameterizedType) {
			Type typeArg = ((ParameterizedType) lastOutType).getActualTypeArguments()[0];
			this.outputType = typeArg;
			return typeArg;
		} else if(lastOutType instanceof Class<?>) {
			Class<?> cl = (Class<?>) lastOutType;
			if(cl.isArray()) {
				Class<?> compType = cl.getComponentType();
				this.outputType = compType;
				return compType;
			}
		}
		
		this.outputType = Object.class;
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
						throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
					}
				} else {
					throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
				}
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
			}
		}
	}
}
