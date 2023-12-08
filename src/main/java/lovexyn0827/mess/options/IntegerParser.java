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

	@Override
	public String getAvailableValues(boolean chinese) {
		return chinese ? "任意整数" : "Any integer";
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

		@Override
		public String getAvailableValues(boolean chinese) {
			return chinese ? "任意正整数" : "Any positive integer";
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

		@Override
		public String getAvailableValues(boolean chinese) {
			return chinese ? "任意非负整数" : "Any non-negative integer";
		}
	}

	public static class HotbarLength extends IntegerParser {
		@Override
		public Integer tryParse(String str) throws InvalidOptionException {
			Integer i = super.tryParse(str);
			if(i > 0 && i <= 36) {
				return i;
			} else {
				throw new InvalidOptionException("Use a number between 1 and 36 here"); // FIXME
			}
		}

		@Override
		public String getAvailableValues(boolean chinese) {
			return chinese ? "1至36间的一个整数（含两端）" : "Any integer between 1 and 36 (inclusive)";
		}
	}
}
