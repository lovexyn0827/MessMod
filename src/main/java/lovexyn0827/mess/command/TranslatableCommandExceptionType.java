package lovexyn0827.mess.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.util.i18n.I18N;

public final class TranslatableCommandExceptionType implements CommandExceptionType {
	private final String translationKey;
	
	public TranslatableCommandExceptionType(String translationKey) {
		this.translationKey = translationKey;
	}

	public CommandSyntaxException create() {
		return new CommandSyntaxException(this, I18N.translateAsText(this.translationKey));
	}
	
	public CommandSyntaxException create(Object ... args) {
		return new CommandSyntaxException(this, I18N.translateAsText(this.translationKey, args));
	}

	public CommandSyntaxException createWithContext(StringReader in) {
		return new CommandSyntaxException(this, I18N.translateAsText(this.translationKey), 
				in.getString(), in.getCursor());
	}
	
	public CommandSyntaxException createWithContext(StringReader in, Object ... args) {
		return new CommandSyntaxException(this, I18N.translateAsText(this.translationKey, args), 
				in.getString(), in.getCursor());
	}
}
