package lovexyn0827.mess.options;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.options.OptionManager.CustomOptionApplicator;
import lovexyn0827.mess.util.i18n.I18N;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;

public final class OptionWrapper {
	final Field field;
	final String name;
	final OptionParser<?> parser;
	final Option option;
	
	OptionWrapper(Field f) {
		this.field = f;
		this.name = f.getName();
		this.option = f.getAnnotation(Option.class);
		this.parser = OptionParser.of(this.option);
	}
	
	public Object get() {
		try {
			return this.field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void set(Object o, @Nullable CommandContext<ServerCommandSource> ct) {
		try {
			this.field.set(null, o);
			CustomOptionApplicator action = OptionManager.CUSTOM_APPLICATION_BEHAVIORS.get(name);
			if(action != null) {
				action.onOptionUpdate(o, ct);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getDescription() {
		return I18N.translate("opt." + this.name + ".desc");
	}
	
	public boolean isSupportedInCurrentEnv() {
		EnvType currentEnv = FabricLoader.getInstance().getEnvironmentType();
		for(EnvType env : this.option.environment()) {
			if(env == currentEnv) {
				return true;
			}
		}
		
		return false;
	}

	public String getDefaultValue() {
		return this.option.defaultValue();
	}

	public SuggestionProvider<ServerCommandSource> getSuggestions() {
		return (ct, b) -> {
			this.parser.createSuggestions().forEach(b::suggest);
			for(String s : this.option.suggestions()) {
				b.suggest(s);
			}
			
			return b.buildFuture();
		};
	}
	
	public boolean isExperimental() {
		return this.option.experimental();
	}

	public boolean globalOnly() {
		return this.option.globalOnly();
	}
}
