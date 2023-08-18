package lovexyn0827.mess.command;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.NameFilter;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

public class EnumSetArgumentType extends ElementSetArgumentType<Enum<?>, EnumSetArgumentType.ParseResult> {
	private final Class<? extends Enum<?>> clazz;
	
	private EnumSetArgumentType(Class<? extends Enum<?>> clazz) {
		this.clazz = clazz;
	}
	
	public static EnumSetArgumentType of(Class<? extends Enum<?>> clazz) {
		return new EnumSetArgumentType(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<?>> Set<T> getEnums(final CommandContext<?> context, final String name) {
		return (Set<T>) context.getArgument(name, ParseResult.class).set;
	}

	@Override
	protected ParseResult filter(NameFilter filter) {
		return new ParseResult(filter.filter(this.clazz));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		for(Enum<?> e : this.clazz.getEnumConstants()) {
			if(e.name().startsWith(builder.getRemaining())) {
				builder.suggest(e.name());
			}
		}
		
		return builder.buildFuture();
	}
	
	static {
		ArgumentTypes.register("mess_enum", EnumSetArgumentType.class, new Serializer());
	}

	static final class ParseResult extends ElementSetArgumentType.ParseResult<Enum<?>> {
		public ParseResult(Set<Enum<?>> set) {
			super(set);
		}
	}
	
	private static class Serializer implements ArgumentSerializer<EnumSetArgumentType> {
		@Override
		public void toPacket(EnumSetArgumentType type, PacketByteBuf buf) {
			buf.writeString(type.clazz.getName());
		}

		@SuppressWarnings("unchecked")
		@Override
		public EnumSetArgumentType fromPacket(PacketByteBuf buf) {
			try {
				return of((Class<? extends Enum<?>>) Class.forName(buf.readString()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return of(Empty.class);
			}
		}

		@Override
		public void toJson(EnumSetArgumentType type, JsonObject jsonObject) {
			jsonObject.addProperty("clazz", type.clazz.getName());
		}
		
	}
	
	public enum Empty {
	}
}
