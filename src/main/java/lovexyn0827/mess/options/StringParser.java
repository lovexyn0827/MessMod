package lovexyn0827.mess.options;

public class StringParser implements OptionParser<String> {

	@Override
	public String tryParse(String str) throws InvaildOptionException {
		return str;
	}

	@Override
	public String serialize(String val) {
		return val;
	}

}
