package lovexyn0827.mess.options;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class EnumParser<T extends Enum<T>> implements OptionParser<Enum<T>> {

	private Class<T> clazz;
	
	protected EnumParser(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public String serialize(Enum<T> val) {
		return val.name();
	}

	@Override
	public Enum<T> tryParse(String str) throws InvalidOptionException {
		try {
			return Enum.valueOf(clazz, str);
		} catch(IllegalArgumentException e) {
			InvalidOptionException e1 = new InvalidOptionException("opt.err.nodef", str);
			e1.initCause(e);
			throw e1;
		}
	}

	@Override
	public Set<String> createSuggestions() {
		return Stream.of(this.clazz.getEnumConstants())
				.map(Enum::name)
				.collect(HashSet::new, Set::add, Set::addAll);
	}
}
