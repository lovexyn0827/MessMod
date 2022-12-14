package lovexyn0827.mess.util.access;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import lovexyn0827.mess.util.Reflection;

class ValueOfMapNode extends Node {

	private Literal<?> keyLiteral;
	private Object key;
	private Type keyType;
	
	ValueOfMapNode(Literal<?> key) {
		this.keyLiteral = key;
	}
	
	@Override
	Object access(Object previous) throws AccessingFailureException {
		if(previous instanceof Map<?, ?>) {
			Map<?, ?> m = (Map<?, ?>) previous;
			if(m.containsKey(this.key)) {
				return m.get(this.key);
			} else {
				throw AccessingFailureException.create(FailureCause.NO_KEY, this);
			}
		} else {
			throw AccessingFailureException.createWithArgs(FailureCause.NOT_MAP, this, null, this);
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
		String keyStr = this.keyLiteral.stringRepresentation;;
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
	protected Type prepare(Type lastOutType) throws AccessingFailureException, InvalidLiteralException {
		if(lastOutType instanceof ParameterizedType) {
			ParameterizedType pt = ((ParameterizedType) lastOutType);
			Class<?> lastCl = Reflection.getRawType(lastOutType);
			if(isObject2PrimitiveMap(lastCl, pt.getActualTypeArguments().length)) {
				Type keyType = pt.getActualTypeArguments()[0];
				this.key = this.keyLiteral.get(keyType);
				Type valType = void.class;
				this.keyType = keyType;
				this.outputType = valType;
				return valType;
			} else if(isPrimitive2ObjectMap(lastCl, pt.getActualTypeArguments().length)){
				Type keyType = void.class;
				this.key = this.keyLiteral.get(keyType);
				Type valType = pt.getActualTypeArguments()[0];
				this.outputType = valType;
				this.keyType = keyType;
				return valType;
			} else {
				Type keyType = pt.getActualTypeArguments()[0];
				this.key = this.keyLiteral.get(keyType);
				Type valType = ((ParameterizedType) lastOutType).getActualTypeArguments()[1];
				this.outputType = valType;
				this.keyType = keyType;
				return valType;
			}
		} else {
			this.key = this.keyLiteral.get(Object.class);
			this.outputType = Object.class;
			this.keyType = Object.class;
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

	@Override
	void uninitialize() {
		super.uninitialize();
		this.key = null;
		this.keyLiteral = this.keyLiteral.recreate();
	}

	@Override
	Node createCopyForInput(Object input) {
		ValueOfMapNode node = new ValueOfMapNode(this.keyLiteral.recreate());
		node.ordinary = this.ordinary;
		return node;
	}
	
	@Override
	boolean isWrittable() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	void write(Object writeTo, Object newValue) throws AccessingFailureException {
		if(ImmutableMap.class.isAssignableFrom(Reflection.getRawType(inputType))) {
			throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
		} else {
			if(writeTo instanceof Map) {
				@SuppressWarnings("rawtypes")
				Map m = (Map) writeTo;
				boolean keyTypeMatched = (this.keyType == void.class || this.key == null) ? true : 
						Reflection.getRawType(this.keyType).isAssignableFrom(this.key.getClass());
				boolean valTypeMatched = (this.outputType == void.class || newValue == null) ? true : 
						Reflection.getRawType(this.outputType).isAssignableFrom(newValue.getClass());
				if(keyTypeMatched && valTypeMatched) {
					try {
						m.put(this.key, newValue);
					} catch (UnsupportedOperationException e) {
						throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
					}
				} else {
					throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
				}
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.NOT_MAP, this, null, this);
			}
		}
	}
}
