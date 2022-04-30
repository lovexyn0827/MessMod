package lovexyn0827.mess.options;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;

/**
 * @author lovexyn0827
 * Date: April 2, 2022
 * @param <T> The type of the option.
 */
public interface OptionParser<T> {
	/**
	 * Translate a string representation of a value of an option to its runtime representation.
	 * @return The runtime representation of {@code str}
	 * @throws InvaildOptionException If the given string representation is not qualified.
	 */
	T tryParse(String str) throws InvaildOptionException;
	
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
	default SuggestionProvider<ServerCommandSource> createSuggestions() {
		return null;
	}
}
