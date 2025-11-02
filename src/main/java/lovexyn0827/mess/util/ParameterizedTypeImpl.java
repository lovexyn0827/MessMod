package lovexyn0827.mess.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {
	private final Type[] actualTypeArguments;
	private final Type rawType;

	public ParameterizedTypeImpl(Type rawType, Type ... actualTypeArguments) {
		this.actualTypeArguments = actualTypeArguments;
		this.rawType = rawType;
	}
	
	@Override
	public Type[] getActualTypeArguments() {
		return this.actualTypeArguments.clone();
	}

	@Override
	public Type getRawType() {
		return this.rawType;
	}

	@Override
	public Type getOwnerType() {
		return null;
	}

}
