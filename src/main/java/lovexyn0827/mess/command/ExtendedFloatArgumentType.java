package lovexyn0827.mess.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class ExtendedFloatArgumentType implements ArgumentType<Float> {

	@Override
	public Float parse(StringReader reader) throws CommandSyntaxException {
		String str = reader.readUnquotedString();
		try {
			return Float.parseFloat(str);
		} catch (NumberFormatException e) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat().createWithContext(reader, str);
		}
	}

	public static ExtendedFloatArgumentType floatArg() {
		return new ExtendedFloatArgumentType();
	}

	public static float getFloat(CommandContext<ServerCommandSource> ct, String string) {
		return ct.getArgument(string, Float.class);
	}
}
