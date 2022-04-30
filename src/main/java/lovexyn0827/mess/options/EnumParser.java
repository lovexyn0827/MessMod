package lovexyn0827.mess.options;

import java.util.stream.Stream;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;

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
	public Enum<T> tryParse(String str) throws InvaildOptionException {
		try {
			return Enum.valueOf(clazz, str);
		} catch(IllegalArgumentException e) {
			InvaildOptionException e1 = new InvaildOptionException("The value of this option couldn't be " + 
					str + ", beacuse " + e.getMessage());
			e1.initCause(e);
			throw e1;
		}
	}

	@Override
	public SuggestionProvider<ServerCommandSource> createSuggestions() {
		return (ct, builder) -> {
			Stream.of(this.clazz.getEnumConstants())
					.map(Enum::name)
					.forEach(builder::suggest);
			return builder.buildFuture();
		};
	}
}
