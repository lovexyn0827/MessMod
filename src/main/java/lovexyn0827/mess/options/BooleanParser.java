package lovexyn0827.mess.options;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.command.CommandUtil;
import net.minecraft.server.command.ServerCommandSource;

public class BooleanParser implements OptionParser<Boolean> {

	@Override
	public Boolean tryParse(String str) throws InvaildOptionException {
		if("true".equals(str)) {
			return true;
		} else if ("false".equals(str)) {
			return false;
		} else {
			throw new InvaildOptionException("Use true of false here");
		}
	}

	@Override
	public String serialize(Boolean val) {
		return val ? "true" : "false";
	}

	@Override
	public SuggestionProvider<ServerCommandSource> createSuggestions() {
		return CommandUtil.immutableSuggestions("true", "false");
	}
}
