package lovexyn0827.mess.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties;
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
		return (Set<T>) context.getArgument(name, FilteredSetArgumentType.ParseResult.class).set;
	}
	
	@Override
	protected FilteredSetArgumentType.ParseResult<T> filter(NameFilter filter) {
		return new FilteredSetArgumentType.ParseResult<T>(
				Sets.newHashSet(filter.filterByKey(this.elementsByName, (a) -> a).values()));
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

	static final class ParseResult<T> extends ElementSetArgumentType.ParseResult<T> {
		public ParseResult(Set<T> set) {
			super(set);
		}
	}
	
	public static class Serializer<T> implements ArgumentSerializer<FilteredSetArgumentType<T>, Prop<T>> {
		@Override
		public void writePacket(Prop<T> prop, PacketByteBuf buf) {
			buf.writeInt(prop.elements.size());
			prop.elements.forEach(buf::writeString);
		}

		@Override
		public Prop<T> fromPacket(PacketByteBuf buf) {
			int count = buf.readInt();
			Set<String> elements = new HashSet<>();
			for(int i = 0; i < count; i++) {
				elements.add(buf.readString());
			}
			
			return new Prop<T>(elements);
		}

		@Override
		public void writeJson(Prop<T> prop, JsonObject json) {
			JsonArray list = new JsonArray();
			prop.elements.forEach(list::add);
			json.add("elements", list);
		}

		@Override
		public Prop<T> getArgumentTypeProperties(FilteredSetArgumentType<T> arg) {
			return new Prop<>(arg);
		}
		
	}
	
	public static class Prop<T> implements ArgumentTypeProperties<FilteredSetArgumentType<T>> {
		protected final Set<String> elements;
		
		public Prop(FilteredSetArgumentType<T> t) {
			this.elements = t.elementsByName.keySet();
		}
		
		public Prop(Set<String> set) {
			this.elements = set;
		}

		@SuppressWarnings("unchecked")
		@Override
		public FilteredSetArgumentType<T> createType(CommandRegistryAccess var1) {
			return (FilteredSetArgumentType<T>) of(this.elements, (e) -> e);
		}

		@Override
		public Serializer<T> getSerializer() {
			return new Serializer<>();
		}
	}
}
