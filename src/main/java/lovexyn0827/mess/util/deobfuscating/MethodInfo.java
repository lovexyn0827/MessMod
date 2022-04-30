package lovexyn0827.mess.util.deobfuscating;

public class MethodInfo {
	public final String srgName;
	public final String name;
	
	/**
	 * If class names are contained, their srg names are used
	 */
	public final String descriptor;
	
	public MethodInfo(String srgName, String name, String descriptor) {
		this.srgName = srgName;
		this.name = name;
		this.descriptor = descriptor;
		
	}
}
