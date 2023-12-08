package lovexyn0827.mess.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.NameFilter;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class FilteredSetArgumentType<T> extends ElementSetArgumentType<T, FilteredSetArgumentType.ParseResult<T>> {
	private final Map<String, T> elementsByName;
	
	private FilteredSetArgumentType(Map<String, T> elementsByName) {
		this.elementsByName = elementsByName;
	}
	
	public static <T> FilteredSetArgumentType<T> of(Set<T> set, Function<? super T, String> toString) {
		return new FilteredSetArgumentType<T>(set.stream()
				.<Map<String, T>>collect(() -> new HashMap<String, T>(), 
						(map, v) -> map.put(toString.apply(v), v), Map::putAll));
	}
	
	public static <T> FilteredSetArgumentType<T> of(Map<String, T> elementsByName) {
		return new FilteredSetArgumentType<T>(elementsByName);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> getFiltered(final CommandContext<?> context, final String name) {
		return (Set<T>) context.getArgument(name, ParseResult.class).set;
	}
	
	@Override
	protected ParseResult<T> filter(NameFilter filter) {
		return new ParseResult<T>(Sets.newHashSet(filter.filterByKey(this.elementsByName, (a) -> a).values()));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		for(String e : this.elementsByName.keySet()) {
			if(e.startsWith(builder.getRemaining())) {
				builder.suggest(e);
			}
		}
		
		return builder.buildFuture();
	}
	
	public static void registerArgumentType() {
		// ???
		@SuppressWarnings("unchecked")
		Class<FilteredSetArgumentType<Object>> cl = 
				(Class<FilteredSetArgumentType<Object>>)(Object) FilteredSetArgumentType.class;
		ArgumentTypes.register("mess_filter", cl, new Serializer<Object>());
	}

	static final class ParseResult<T> extends ElementSetArgumentType.ParseResult<T> {
		public ParseResult(Set<T> set) {
			super(set);
		}
	}
	
	private static class Serializer<T> implements ArgumentSerializer<FilteredSetArgumentType<T>> {
		@Override
		public void toPacket(FilteredSetArgumentType<T> type, PacketByteBuf buf) {
			buf.writeInt(type.elementsByName.size());
			type.elementsByName.keySet().forEach(buf::writeString);
		}

		@SuppressWarnings("unchecked")
		@Override
		public FilteredSetArgumentType<T> fromPacket(PacketByteBuf buf) {
			int count = buf.readInt();
			Map<String, T> elements = new HashMap<>();
			for(int i = 0; i < count; i++) {
				elements.put(buf.readString(), (T) new Object());
			}
			
			return of(elements);
		}

		@Override
		public void toJson(FilteredSetArgumentType<T> type, JsonObject jsonObject) {
			JsonArray list = new JsonArray();
			type.elementsByName.keySet().forEach(list::add);
			jsonObject.add("elements", list);
		}
		
	}
}
