package lovexyn0827.mess.options;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * @author lovexyn0827
 * Date: April 2, 2022
 * @param <T> The type of the option.
 */
public interface OptionParser<T> {
	/**
	 * Translate a string representation of a value of an option to its runtime representation.
	 * @return The runtime representation of {@code str}
	 * @throws InvalidOptionException If the given string representation is not qualified.
	 */
	T tryParse(String str) throws InvalidOptionException;
	
	/**
	 * Translate a runtime representation of a value of an option to its string representation.
	 * @return The runtime representation of {@code val}
	 */
	String serialize(T val);
	
	@SuppressWarnings("unchecked")
	default String serializeObj(Object val) {
		return serialize((T) val);
	}
	
	@Nullable
	default Set<String> createSuggestions() {
		return null;
	}
	
	default void validate(String in) throws InvalidOptionException {
		this.tryParse(in);
	}
	
	static OptionParser<?> of(Option o) {
		try {
			return o.parserClass().getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	static OptionParser<?> of(String optionName) {
		OptionWrapper o = OptionManager.OPTIONS.get(optionName);
		if(o != null) {
			return of(o.option);
		} else {
			return null;
		}
	}
}
