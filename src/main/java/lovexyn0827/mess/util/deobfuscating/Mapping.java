package lovexyn0827.mess.util.deobfuscating;

public interface Mapping {
	String namedClass(String srg);
	/**
	 * Get the srg name of a class from its deobfuscated name
	 * @param named The deobfuscated binary name of the class
	 */
	String srgClass(String named);
	String namedField(String srg);
	/**
	 * @param clazz Use the srg name of the target class
	 */
	String srgField(String clazz, String named);
	
	String namedMethod(String srg, String desc);
	
	/**
	 * @implSpec 
	 * @param clazz
	 * @param named
	 * @param desc
	 * @return
	 */
	String srgMethod(String clazz, String named, String desc);
	
	boolean isClassMapped(Class<?> clazz);
	
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
	
	default String srgMethodRecursively(Class<?> targetClass, String fieldName, String desc) {
		if(this.isDummy()) {
			return fieldName;
		}
		
		while(targetClass != Object.class) {
			String srg = this.srgMethod(targetClass.getName(), fieldName, desc);
			if(srg != null) {
				return srg;
			}
			
			targetClass = targetClass.getSuperclass();
		}
		
		return fieldName;
	}
	
	default boolean isDummy() {
		return this instanceof DummyMapping;
	}
}
