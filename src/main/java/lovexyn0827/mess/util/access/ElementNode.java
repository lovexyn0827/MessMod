package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

public class ElementNode extends Node {

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
				throw new AccessingFailureException(AccessingFailureException.Cause.OUT_OF_BOUND, this);
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
				throw new AccessingFailureException(AccessingFailureException.Cause.OUT_OF_BOUND, this);
			}
		} else {
			throw new AccessingFailureException(AccessingFailureException.Cause.BAD_INPUT, this);
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
				&& (this.outputType == null && other.outputType == null || this.outputType.equals(other.outputType));
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

}
