package lovexyn0827.mess.options;

public class IntegerParser implements OptionParser<Integer> {
	@Override
	public Integer tryParse(String str) throws InvalidOptionException {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException e) {
			throw new InvalidOptionException("opt.err.rint");
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
				throw new InvalidOptionException("opt.err.rpositive");
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
				throw new InvalidOptionException("opt.err.rnonnegative");
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
				throw new InvalidOptionException("opt.err.rhotbar");
			}
		}
	}
}
