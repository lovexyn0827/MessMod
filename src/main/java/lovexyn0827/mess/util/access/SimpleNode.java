package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.function.Function;

import lovexyn0827.mess.util.Reflection;

class SimpleNode<I, O> extends Node {
	static final SimpleNode<Object, Integer> IDENTITY_HASH = 
			new SimpleNode<>((ob) -> System.identityHashCode(ob), "identityHash", Object.class, Integer.class);
	private final Function<I, O> func;
	private final String name;
	private final Class<?> in;
	private final Class<?> out;

	// This constructor is set to private to make equals() work properly by avoiding duplication
	private SimpleNode(Function<I, O> func, String name, Class<? extends I> in, Class<? super O> out) {
		this.func = func;
		this.name = name;
		this.in = in;
		this.out = out;
	}
	
	@SuppressWarnings("unchecked")	// FIXME
	@Override
	Object access(Object previous) {
		return func.apply((I) previous);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	boolean canFollow(Node n) {
		Class<?> last = Reflection.getRawType(n.outputType);
		return last == null || this.in.isAssignableFrom(last);
	}

	@Override
	protected Type prepare(Type lastOutType) {
		return this.outputType = out;
	}
}
