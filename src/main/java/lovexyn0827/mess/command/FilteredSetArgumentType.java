package lovexyn0827.mess.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.NameFilter;

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
		return (Set<T>) context.getArgument(name, lovexyn0827.mess.command.FilteredSetArgumentType.ParseResult.class).set;
	}
	
	@Override
	protected lovexyn0827.mess.command.FilteredSetArgumentType.ParseResult<T> filter(NameFilter filter) {
		return new lovexyn0827.mess.command.FilteredSetArgumentType.ParseResult<T>(Sets.newHashSet(filter.filterByKey(this.elementsByName, (a) -> a).values()));
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
}
