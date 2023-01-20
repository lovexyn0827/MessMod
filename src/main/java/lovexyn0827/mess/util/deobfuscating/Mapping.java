package lovexyn0827.mess.util.deobfuscating;

public interface Mapping {
	/**
	 * Get the deobfuscated name of a class from its srg name
	 * @param named The deobfuscated binary name of the class
	 */
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
	
	default String srgMethodRecursively(Class<?> targetClass, String name, String desc) {
		if(this.isDummy() || !this.isClassMapped(targetClass)) {
			return name;
		}
		
		while(targetClass != Object.class) {
			String srg = this.srgMethod(targetClass.getName(), name, desc);
			if(srg != null) {
				return srg;
			}
			
			targetClass = targetClass.getSuperclass();
		}
		
		return name;
	}
	
	default boolean isDummy() {
		return this instanceof DummyMapping;
	}
	
	default String simpleNamedClass(String srg) {
		String named = this.namedClass(srg);
		return named.substring(named.lastIndexOf('.') + 1, named.length());
	}
}
