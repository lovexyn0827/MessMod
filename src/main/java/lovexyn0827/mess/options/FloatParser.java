package lovexyn0827.mess.options;

public class FloatParser implements OptionParser<Float> {

	@Override
	public Float tryParse(String str) throws InvaildOptionException {
		try {
			Float f = Float.valueOf(str);
			if(f > 0) {
				return f;
			} else {
				throw new InvaildOptionException("Use a positive number here");
			}
		} catch (NumberFormatException e) {
			throw new InvaildOptionException("The given value is not a number!");
		}
	}

	@Override
	public String serialize(Float val) {
		return Float.toString(val);
	}

	public static class Positive extends FloatParser {
		@Override
		public Float tryParse(String str) throws InvaildOptionException {
			Float f = super.tryParse(str);
			if(f > 0) {
				return f;
			} else {
				throw new InvaildOptionException("Use a positive number here");
			}
		}
	}
}
