package lovexyn0827.mess.options;

import java.util.Set;

import com.google.common.collect.Sets;

public class BooleanParser implements OptionParser<Boolean> {

	@Override
	public Boolean tryParse(String str) throws InvalidOptionException {
		if("true".equals(str)) {
			return true;
		} else if ("false".equals(str)) {
			return false;
		} else {
			throw new InvalidOptionException("opt.err.reqbool");
		}
	}

	@Override
	public String serialize(Boolean val) {
		return val ? "true" : "false";
	}

	@Override
	public Set<String> createSuggestions() {
		return Sets.newHashSet("true", "false");
	}
}
