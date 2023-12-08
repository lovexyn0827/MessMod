package lovexyn0827.mess.options;

public class StringParser implements OptionParser<String> {

	@Override
	public String tryParse(String str) throws InvalidOptionException {
		return str;
	}

	@Override
	public String serialize(String val) {
		return val;
	}
	
	@Override
	public String getAvailableValues(boolean chinese) {
		return chinese ? "任意字符串" : "Any string";
	}

}
