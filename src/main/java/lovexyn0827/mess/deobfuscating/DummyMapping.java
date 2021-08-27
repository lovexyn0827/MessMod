package lovexyn0827.mess.deobfuscating;

public class DummyMapping implements Mapping {

	@Override
	public String namedClass(String srg) {
		return srg;
	}

	@Override
	public String namedField(String srg) {
		return srg;
	}

	/*@Override
	public String namedMethod(String srg) {
		return srg;
	}*/
}
