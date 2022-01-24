package lovexyn0827.mess.deobfuscating;

import java.lang.reflect.Field;

// Methods are not supported yet
public interface Mapping {
	String namedClass(String srg);
	String namedField(String srg);
	String srgClass(String named);
	
	/**
	 * @param clazz Use the srg name of the target class
	 */
	String srgField(String clazz, String named);
	//String namedMethod(String srg);
	
	default String srgFieldRecursively(Class<?> targetClass, String fieldName) {
		if(this.isDummy()) {
			return fieldName;
		}
		
		while(targetClass != Object.class) {
			String srg = this.srgField(targetClass.getName(), fieldName);
			if(srg != null) {
				return srg;
			}
			
			targetClass = targetClass.getSuperclass();
		}
		
		return fieldName;
	}
	
	/**
	 * @param fieldName Use the srg name
	 * @return A Field instance if the specified field exists in the given class or its super classes, null otherwise.
	 */
	default Field getFieldFromNamed(Class<?> targetClass, String fieldName) {
		while(targetClass != Object.class) {
			String srg = this.srgField(targetClass.getName(), fieldName);
			if(srg != null) {
				try {
					return targetClass.getDeclaredField(srg);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (SecurityException e) {}
			}
			
			targetClass = targetClass.getSuperclass();
		}
		
		return null;
	}
	
	default boolean isDummy() {
		return this instanceof DummyMapping;
	}
}
