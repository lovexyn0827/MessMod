package lovexyn0827.mess.util.deobfuscating;

import org.jetbrains.annotations.NotNull;

/**
 * @implSpec The input, instead of {@code null} should be returned if the corresponding entries doesn't exist.
 */
public interface Mapping {
	/**
	 * Get the deobfuscated name of a class from its srg name
	 * @param named The deobfuscated binary name of the class
	 */
	@NotNull
	String namedClass(String srg);
	
	/**
	 * Get the srg name of a class from its deobfuscated name
	 * @param named The deobfuscated binary name of the class
	 */
	@NotNull
	String srgClass(String named);
	
	@NotNull
	String namedField(String srg);
	
	/**
	 * @param clazz Use the srg name of the target class
	 */
	@NotNull
	String srgField(String clazz, String named);
	
	@NotNull
	String namedMethod(String srg, String desc);
	
	/**
	 * @param clazz
	 * @param named
	 * @param desc
	 * @return
	 */
	@NotNull
	String srgMethod(String clazz, String named, String desc);
	
	boolean isClassMapped(Class<?> clazz);
	
	@NotNull
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
	
	@NotNull
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
	
	@NotNull
	default String simpleNamedClass(String srg) {
		String named = this.namedClass(srg);
		return named.substring(named.lastIndexOf('.') + 1, named.length());
	}
}
