package lovexyn0827.mess.options;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

public class DelegatableBooleamParser implements OptionParser<Optional<Boolean>> {
	public static final String DELEGATE_STR = "inherit";
	
	@Override
	public Optional<Boolean> tryParse(String str) throws InvalidOptionException {
		if (DELEGATE_STR.equals(str)) {
			return Optional.empty();
		} else {
			try {
				return Optional.of(Boolean.parseBoolean(str));
			} catch (IllegalArgumentException e) {
				throw new InvalidOptionException("opt.err.reqdelegbool");
			}
		}
	}

	@Override
	public String serialize(Optional<Boolean> val) {
		if (val.isPresent()) {
			return Boolean.toString(val.get());
		} else {
			return DELEGATE_STR;
		}
	}

	@Override
	public Set<String> createSuggestions() {
		return Sets.newHashSet("true", "false", DELEGATE_STR);
	}
}
