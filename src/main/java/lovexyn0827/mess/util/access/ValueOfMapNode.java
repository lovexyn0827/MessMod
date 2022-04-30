package lovexyn0827.mess.util.access;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.Reflection;

class ValueOfMapNode extends Node {

	private Literal<?> keyLiteral;
	private Object key;
	
	ValueOfMapNode(Literal<?> key) {
		this.keyLiteral = key;
	}
	
	@Override
	Object access(Object previous) {
		if(previous instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) previous;
			if(m.containsKey(this.key)) {
				return m.get(this.key);
			} else {
				throw new TranslatableException("exp.nokey");
			}
		} else {
			throw new TranslatableException("exp.notmap", this);
		}
	}
	
	@Override
	public int hashCode() {
		return this.key.hashCode() ^ (this.outputType != null ? this.outputType.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || ValueOfMapNode.class != obj.getClass()) {
			return false;
		}
		
		ValueOfMapNode other = (ValueOfMapNode) obj;
		return this.key.equals(other.key) 
				&& (this.outputType == null && other.outputType == null || this.outputType.equals(other.outputType));
	}
	
	@Override
	public String toString() {
		String keyStr = this.key.toString();
		if(this.key instanceof CharSequence) {
			keyStr = '"' + keyStr + '"';
		}
		
		return '<' + keyStr + '>';
	}
	
	@Override
	boolean canFollow(Node n) {
		return Map.class.isAssignableFrom(Reflection.getRawType(n.outputType));
	}

	@Override
	protected Type prepare(Type lastOutType) {
		if(lastOutType instanceof ParameterizedType) {
			ParameterizedType pt = ((ParameterizedType) lastOutType);
			Class<?> lastCl = Reflection.getRawType(lastOutType);
			// TODO Primitive2Object & Object2Primitive Maps
			if(isObject2PrimitiveMap(lastCl, pt.getActualTypeArguments().length)) {
				Type keyType = pt.getActualTypeArguments()[0];
				this.key = this.keyLiteral.get(keyType);
				Type valType = void.class;
				this.outputType = valType;
				return valType;
			} else if(isPrimitive2ObjectMap(lastCl, pt.getActualTypeArguments().length)){
				Type keyType = void.class;
				this.key = this.keyLiteral.get(keyType);
				Type valType = pt.getActualTypeArguments()[0];
				this.outputType = valType;
				return valType;
			} else {
				Type keyType = pt.getActualTypeArguments()[0];
				this.key = this.keyLiteral.get(keyType);
				Type valType = ((ParameterizedType) lastOutType).getActualTypeArguments()[1];
				this.outputType = valType;
				return valType;
			}
		} else {
			this.key = this.keyLiteral.get(Object.class);
			this.outputType = Object.class;
			return Object.class;
		}
	}
	
	private static boolean isFastutilClass(Class<?> cl) {
		return cl.getName().startsWith("it.unimi.dsi.fastutil.");
	}

	private static boolean isPrimitive2ObjectMap(Class<?> cl, int length) {
		return isFastutilClass(cl) && length == 1 && cl.getName().contains("2Object");
	}

	private static boolean isObject2PrimitiveMap(Class<?> cl, int length) {
		return isFastutilClass(cl) && length == 1 && cl.getName().contains("Object2");
	}

}
