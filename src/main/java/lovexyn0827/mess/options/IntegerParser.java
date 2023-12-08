package lovexyn0827.mess.options;

public class IntegerParser implements OptionParser<Integer> {
	@Override
	public Integer tryParse(String str) throws InvalidOptionException {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("The given value is not a number!");
		}
	}

	@Override
	public String serialize(Integer val) {
		return Integer.toString(val);
	}

	public static class Positive extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvalidOptionException {
			Integer i = super.tryParse(str);
			if(i > 0) {
				return i;
			} else {
				throw new InvalidOptionException("Use a positive number here");
			}
		}
	}
	
	public static class NonNegative extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvalidOptionException {
			Integer i = super.tryParse(str);
			if(i >= 0) {
				return i;
			} else {
				throw new InvalidOptionException("Use a non-negative number here");
			}
		}
	}

	public static class HotbarLength extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvalidOptionException {
			Integer i = super.tryParse(str);
			if(i > 0 && i <= 36) {
				return i;
			} else {
				throw new InvalidOptionException("Use a number between 1 and 36 here");
			}
		}
	}
}
