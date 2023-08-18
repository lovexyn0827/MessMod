package lovexyn0827.mess.options;

public class FloatParser implements OptionParser<Float> {

	@Override
	public Float tryParse(String str) throws InvalidOptionException {
		try {
			return Float.valueOf(str);
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("The given value is not a number!");
		}
	}

	@Override
	public String serialize(Float val) {
		return Float.toString(val);
	}

	public static class Positive extends FloatParser {
		@Override
		public Float tryParse(String str) throws InvalidOptionException {
			Float f = super.tryParse(str);
			if(f > 0) {
				return f;
			} else {
				throw new InvalidOptionException("Use a positive number here");
			}
		}
	}
}
