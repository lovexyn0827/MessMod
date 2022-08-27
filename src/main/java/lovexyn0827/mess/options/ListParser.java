package lovexyn0827.mess.options;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.render.debug.DebugRenderer;
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
		
		List<T> result = Lists.newArrayList();
		for(String elementStr : str.split(",")) {
			T element = this.parseElement(elementStr);
			if(element != null) {
				result.add(element);
			} else {
				throw new InvaildOptionException("cmd.general.nodef", elementStr);
			}
		}
		
		return result;
	}

	protected T parseElement(String elementStr) throws InvaildOptionException {
		return this.elements.get(elementStr);
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
	
	public static class DebugRender extends ListParser<Either<Field, String>> {
		private static final ImmutableBiMap<String, Either<Field, String>> VANILLA_DEBUG_RENDERERS;
		
		public DebugRender() {
			super(VANILLA_DEBUG_RENDERERS);
		}
		
		@Override
		public List<Either<Field, String>> tryParse(String str) throws InvaildOptionException {
			return super.tryParse(str);
		}
		
		@Override
		protected Either<Field, String> parseElement(String elementStr) throws InvaildOptionException {
			if(MessMod.isDedicatedServerEnv()) {
				return Either.right(elementStr);
			} else {
				return super.parseElement(elementStr);
			}
		}

		static {
			ImmutableBiMap.Builder<String, Either<Field, String>> builder = ImmutableBiMap.builder();
			if(!MessMod.isDedicatedServerEnv()) {
				Stream.of(DebugRenderer.class.getDeclaredFields())
						.filter((f) -> DebugRenderer.Renderer.class.isAssignableFrom(f.getType()))
						.forEach((f) -> {
							try {
								builder.put(MessMod.INSTANCE.getMapping().namedField(f.getName()), Either.left(f));
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						});
			}
			
			VANILLA_DEBUG_RENDERERS = builder.build();
		}
	}
}

