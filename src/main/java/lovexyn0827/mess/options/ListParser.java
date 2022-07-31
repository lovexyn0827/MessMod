package lovexyn0827.mess.options;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketType;

public abstract class ListParser<T> implements OptionParser<List<? extends T>> {
	public static final String EMPTY_LIST = "[]";
	protected final BiMap<String, T> elements;
	
	public ListParser(BiMap<String, T> elements) {
		this.elements = elements;
	}
	
	@Override
	public List<T> tryParse(String str) throws InvaildOptionException {
		if(EMPTY_LIST.equals(str) || str.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<T> types = Lists.newArrayList();
		for(String typeName : str.split(",")) {
			T type = this.elements.get(typeName);
			if(type != null) {
				types.add(type);
			}
		}
		
		return types;
	}

	@Override
	public String serialize(List<? extends T> val) {
		if (val.isEmpty()) {
			return EMPTY_LIST;
		}
		
		StringBuilder sb = new StringBuilder();
		val.forEach((t) -> {
			sb.append(',').append(this.elements.inverse().get(t));
		});
		
		return sb.charAt(0) == ',' ? sb.deleteCharAt(0).toString() : sb.toString();
	}
	
	@Override
	public SuggestionProvider<ServerCommandSource> createSuggestions() {
		return (ct, b) -> {
			this.elements.keySet().forEach(b::suggest);
			b.suggest(EMPTY_LIST);
			return b.buildFuture();
		};
	}
	
	public static class Ticket extends ListParser<ChunkTicketType<?>> {
		private static final ImmutableBiMap<String, ChunkTicketType<?>> VANILLA_TICKET_TYPES;
		
		public Ticket() {
			super(VANILLA_TICKET_TYPES);
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
