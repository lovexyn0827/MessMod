package lovexyn0827.mess.command;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.NameFilter;

/**
 * @author lovexyn0827
 * @date Aug. 16, 2023
 * @param <T> The type of elements to be filtered
 * @param <R> The type of parsing result which used to store the filtering result
 */
public abstract class ElementSetArgumentType<T, R extends ElementSetArgumentType.ParseResult<T>> 
		implements ArgumentType<R> {
	protected abstract R filter(NameFilter filter);
	
	@Override
	public R parse(StringReader reader) throws CommandSyntaxException {
		int start = reader.getCursor();
		while(reader.canRead()) {
			char c = reader.peek();
			if(Character.isJavaIdentifierPart(c) || c == '*' || c == '?') {
				reader.skip();
			} else {
				break;
			}
		}
		
		return this.filter(NameFilter.compile(reader.getString().substring(start, reader.getCursor())));
	}

	@Override
	public abstract <S> CompletableFuture<Suggestions> listSuggestions(
			CommandContext<S> context, SuggestionsBuilder builder);

	@Override
	public Collection<String> getExamples() {
		return Sets.newHashSet("*", "ENTITY", "EN???Y", "E*T?");
	}

	public static abstract class ParseResult<T> {
		protected final Set<T> set;
		
		public ParseResult(Set<T> set) {
			this.set = set;
		}
	}
}
