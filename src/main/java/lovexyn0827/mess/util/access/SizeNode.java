package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

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
			throw new AccessingFailureException(AccessingFailureException.Cause.BAD_INPUT, this, this);
		}
	}

	@Override
	protected Type prepare(Type lastOutType) {
		this.outputType = int.class;
		return int.class;
	}

}
