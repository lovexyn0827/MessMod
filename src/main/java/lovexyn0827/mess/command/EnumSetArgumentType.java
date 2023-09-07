package lovexyn0827.mess.command;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import lovexyn0827.mess.util.NameFilter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties;
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
	public static <T extends Enum<?>> Set<T> 
			getEnums(final CommandContext<?> context, final String name) {
		return (Set<T>) context.getArgument(name, EnumSetArgumentType.ParseResult.class).set;
	}

	@Override
	protected EnumSetArgumentType.ParseResult filter(NameFilter filter) {
		return new EnumSetArgumentType.ParseResult(filter.filter(this.clazz));
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

	static final class ParseResult extends ElementSetArgumentType.ParseResult<Enum<?>> {
		public ParseResult(Set<Enum<?>> set) {
			super(set);
		}
	}
	
	public enum Empty {
	}
	
	public static class Serializer implements ArgumentSerializer<EnumSetArgumentType, Prop> {
		@Override
		public void writePacket(Prop prop, PacketByteBuf buf) {
			buf.writeString(prop.clazz.getName());
		}

		@SuppressWarnings("unchecked")
		@Override
		public Prop fromPacket(PacketByteBuf buf) {
			try {
				String cn = buf.readString();
				return new Prop((Class<? extends Enum<?>>) Class.forName(cn));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return new Prop(Empty.class);
			}
		}

		@Override
		public void writeJson(Prop prop, JsonObject json) {
			json.addProperty("clazz", prop.clazz.getName());
		}

		@Override
		public Prop getArgumentTypeProperties(EnumSetArgumentType arg) {
			return new Prop(arg);
		}
		
	}
	
	public static class Prop implements ArgumentTypeProperties<EnumSetArgumentType> {
		protected final Class<? extends Enum<?>> clazz;
		
		public Prop(EnumSetArgumentType t) {
			this.clazz = t.clazz;
		}
		
		public Prop(Class<? extends Enum<?>> c) {
			this.clazz = c;
		}

		@Override
		public EnumSetArgumentType createType(CommandRegistryAccess var1) {
			return of(this.clazz);
		}

		@Override
		public Serializer getSerializer() {
			return new Serializer();
		}
	}
}
