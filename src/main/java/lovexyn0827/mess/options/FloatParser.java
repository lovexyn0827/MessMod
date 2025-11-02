package lovexyn0827.mess.options;

public class FloatParser implements OptionParser<Float> {

	@Override
	public Float tryParse(String str) throws InvalidOptionException {
		try {
			return Float.valueOf(str);
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("opt.err.rnum");
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
				throw new InvalidOptionException("opt.err.rpositive");
			}
		}
	}
	
	public static class NonNegative extends FloatParser {
		@Override
		public Float tryParse(String str) throws InvalidOptionException {
			Float f = super.tryParse(str);
			if(f >= 0) {
				return f;
			} else {
				throw new InvalidOptionException("opt.err.rnonnegative");
			}
		}
	}
	
	
	public static class NaNablePositive extends Positive {
		@Override
		public Float tryParse(String str) throws InvalidOptionException {
			if("NaN".equals(str)) {
				return Float.NaN;
			} else {
				return super.tryParse(str);
			}
		}
	}
}
