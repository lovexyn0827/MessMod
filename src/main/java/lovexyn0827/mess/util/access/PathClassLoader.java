package lovexyn0827.mess.util.access;

import lovexyn0827.mess.MessMod;

class PathClassLoader extends ClassLoader {
	public static final PathClassLoader INSTANCE = new PathClassLoader();
	
	private PathClassLoader() {
		super(MessMod.class.getClassLoader());
	}

	public Class<?> defineClass(String name, byte[] bytes) {
		return super.defineClass(name, bytes, 0, bytes.length);
	}
}
