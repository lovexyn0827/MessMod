package lovexyn0827.mess.util.phase;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.server.command.ServerCommandSource;

public class TickingPhaseArgumentType implements ArgumentType<TickingPhase> {
	// TODO Use CommandException more.
	private static final DynamicCommandExceptionType UNKNOWN_PHASE = new DynamicCommandExceptionType((key) -> {
		return () -> I18N.translate("exp.unknownphase", key);
	});

	@Override
	public TickingPhase parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		for(ServerTickingPhase p : ServerTickingPhase.values()) {
			if(p.name().equals(name)) {
				return p;
			}
		}

		if(!MessMod.isDedicatedEnv()) {
			for(ClientTickingPhase p : ClientTickingPhase.values()) {
				if(p.name().equals(name)) {
					return p;
				}
			}
		}
		
		throw UNKNOWN_PHASE.create(name);
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		for(ServerTickingPhase phase : ServerTickingPhase.values()) {
			if (phase.name().contains(builder.getRemaining())) {
				builder.suggest(phase.name());
			}
		}
		
		if(!MessMod.isDedicatedEnv()) {
			for(ClientTickingPhase phase : ClientTickingPhase.values()) {
				if (phase.name().contains(builder.getRemaining())) {
					builder.suggest(phase.name());
				}
			}
		}
		
		return builder.buildFuture();
	}

	public static TickingPhaseArgumentType phaseArg() {
		return new TickingPhaseArgumentType();
	}
	
	public static TickingPhase getPhase(CommandContext<ServerCommandSource> ct, String argName) {
		return ct.getArgument(argName, TickingPhase.class);
	}
}
