package lovexyn0827.mess.util.deobfuscating;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

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
	 * @param desc The descriptor of target method, using intermediate names. 
	 */
	@NotNull
	String srgMethod(String clazz, String named, String desc);
	
	/**
	 * Whether or not a class, or at least one of its members is associated with a named name;
	 */
	boolean isClassMapped(Class<?> clazz);
	
	@NotNull
	default String srgFieldRecursively(Class<?> targetClass, String fieldName) {
		if(this.isDummy()) {
			return fieldName;
		}
		
		while(targetClass != null) {
			String srg = this.srgField(targetClass.getName(), fieldName);
			if(srg != null) {
				return srg;
			}
			
			for(Class<?> in : targetClass.getInterfaces()) {
				srg = this.srgFieldRecursively(in, fieldName);
				if(!srg.equals(fieldName)) {
					return srg;
				}
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
		
		while(targetClass != null) {
			String srg = this.srgMethod(targetClass.getName(), name, desc);
			if(srg != null) {
				return srg;
			}

			for(Class<?> in : targetClass.getInterfaces()) {
				srg = this.srgMethodRecursively(in, name, desc);
				if(!srg.equals(name)) {
					return srg;
				}
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
	
	default String srgDescriptor(String namedDesc) {
		if(namedDesc.isEmpty()) {
			throw new IllegalArgumentException("Descriptors mustn't be empty!");
		}
		
		switch(namedDesc.charAt(0)) {
		case 'I':
		case 'F':
		case 'D':
		case 'Z':
		case 'J':
		case 'B':
		case 'S':
		case 'C':
		case 'V':
			return namedDesc;
		case '[':
			return '[' + this.srgDescriptor(namedDesc.substring(1));
		case 'L':
			return 'L' + this.srgClass(namedDesc.substring(1, namedDesc.length() - 1)
					.replace('/', '.')).replace('.', '/') + ';';
		default :
			throw new IllegalArgumentException("Malformed descriptor: " + namedDesc);
		}
	}
	
	default String srgMethodDescriptor(String namedDesc) {
		Type descType = Type.getMethodType(namedDesc);
		Type[] srgArgTypes = Arrays.stream(descType.getArgumentTypes())
				.map(Type::getInternalName)
				.map(this::srgDescriptor)
				.map(Type::getType)
				.toArray((count) -> new Type[count]);
		return Type.getMethodDescriptor(Type.getType(this.srgDescriptor(descType.getReturnType().getDescriptor())), 
				srgArgTypes);
		
	}
}
