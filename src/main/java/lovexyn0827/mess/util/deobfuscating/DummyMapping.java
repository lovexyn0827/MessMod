package lovexyn0827.mess.util.deobfuscating;

class DummyMapping implements Mapping {

	@Override
	public String namedClass(String srg) {
		return srg;
	}

	@Override
	public String namedField(String srg) {
		return srg;
	}
	
	public String srgClass( String named) {
		return named;
	}

	@Override
	public String srgField(String clazz, String named) {
		return named;
	}

	@Override
	public boolean isClassMapped(Class<?> clazz) {
		return false;
	}

	@Override
	public String namedMethod(String srg, String desc) {
		return srg;
	}

	@Override
	public String srgMethod(String clazz, String named, String desc) {
		return named;
	}
}
