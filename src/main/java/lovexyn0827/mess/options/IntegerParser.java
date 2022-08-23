package lovexyn0827.mess.options;

public class IntegerParser implements OptionParser<Integer> {
	@Override
	public Integer tryParse(String str) throws InvaildOptionException {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException e) {
			throw new InvaildOptionException("The given value is not a number!");
		}
	}

	@Override
	public String serialize(Integer val) {
		return Integer.toString(val);
	}

	public static class Positive extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvaildOptionException {
			Integer i = super.tryParse(str);
			if(i > 0) {
				return i;
			} else {
				throw new InvaildOptionException("Use a positive number here");
			}
		}
	}
	
	public static class NonNegative extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvaildOptionException {
			Integer i = super.tryParse(str);
			if(i >= 0) {
				return i;
			} else {
				throw new InvaildOptionException("Use a non-negative number here");
			}
		}
	}

	public static class HotbarLength extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvaildOptionException {
			Integer i = super.tryParse(str);
			if(i > 0 && i <= 36) {
				return i;
			} else {
				throw new InvaildOptionException("Use a number between 1 and 36 here");
			}
		}
	}
}
