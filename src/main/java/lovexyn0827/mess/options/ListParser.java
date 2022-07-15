package lovexyn0827.mess.options;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketType;

public abstract class ListParser implements OptionParser<List<?>> {
	public static final String EMPTY_LIST = "[]";
	
	public static class Ticket extends ListParser {
		private static final ImmutableBiMap<String, ChunkTicketType<?>> VANILLA_TICKET_TYPES;
		
		@Override
		public List<ChunkTicketType<?>> tryParse(String str) throws InvaildOptionException {
			if(EMPTY_LIST.equals(str)) {
				return Collections.emptyList();
			}
			
			List<ChunkTicketType<?>> types = Lists.newArrayList();
			for(String typeName : str.split(",")) {
				ChunkTicketType<?> type = VANILLA_TICKET_TYPES.get(typeName);
				if(type != null) {
					types.add(type);
				}
			}
			
			return types;
		}

		@Override
		public String serialize(List<?> val) {
			StringBuilder sb = new StringBuilder();
			val.forEach((t) -> {
				sb.append(',').append(VANILLA_TICKET_TYPES.inverse().get(t));
			});
			
			return sb.toString();
		}
		
		@Override
		public SuggestionProvider<ServerCommandSource> createSuggestions() {
			return (ct, b) -> {
				VANILLA_TICKET_TYPES.keySet().forEach(b::suggest);
				b.suggest(EMPTY_LIST);
				return b.buildFuture();
			};
		}
		
		static {
			ImmutableBiMap.Builder<String, ChunkTicketType<?>> builder = ImmutableBiMap.builder();
			Stream.of(ChunkTicketType.class.getDeclaredFields())
					.filter((f) -> Modifier.isStatic(f.getModifiers()))
					.filter((f) -> f.getType().equals(ChunkTicketType.class))
					.map((f) -> {
						try {
							return (ChunkTicketType<?>) f.get(null);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					})
					.forEach((t) -> builder.put(t.toString(), t));
			VANILLA_TICKET_TYPES = builder.build();
		}
	}
}
