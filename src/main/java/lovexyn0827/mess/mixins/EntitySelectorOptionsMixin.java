package lovexyn0827.mess.mixins;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.TranslatableCommandExceptionType;
import lovexyn0827.mess.fakes.EntitySelectorReaderInterface;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {
	private static final TranslatableCommandExceptionType NEGATIVE_ID_EXCEPTION = 
			new TranslatableCommandExceptionType("misc.negativeid");
	private static final DynamicCommandExceptionType UNDEFINED_EXCEPTION = 
			new DynamicCommandExceptionType((side) -> {
				return new LiteralText(I18N.translate("cmd.general.nodef", side));
			});
	private static final DynamicCommandExceptionType INVALID_REGEX_EXCEPTION = 
			new DynamicCommandExceptionType((e) -> {
				String msg;
				if(e instanceof Throwable) {
					msg = ((Throwable) e).getLocalizedMessage();
				} else {
					msg = e.toString();
				}
				
				return new LiteralText(I18N.translate("misc.invregex", msg));
			});
	private static final DynamicCommandExceptionType NO_CLASS_EXCEPTION = 
			new DynamicCommandExceptionType((e) -> {
				return new LiteralText(I18N.translate("exp.noclass", e));
			});
	
	@Shadow
	private static final Map<String, ?> options = Maps.newHashMap();
	@Shadow
	private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, 
			Predicate<EntitySelectorReader> condition, Text description) {}
	
	@Inject(method = "register", at = @At("RETURN"))
    private static void registerOptions(CallbackInfo info) {
		putOption("id", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			NumberRange.IntRange intRange = NumberRange.IntRange.parse(selectorReader.getReader());
			Integer min = intRange.getMin();
			Integer max = intRange.getMax();
			if ((min == null || min >= 0) && (max == null || max >= 0)) {
				((EntitySelectorReaderInterface) selectorReader).setIdRange(intRange);
				if(max != null && min != null && max.equals(min)) {
					selectorReader.setLimit(1);
					selectorReader.setHasLimit(true);
				}
			} else {
				selectorReader.getReader().setCursor(i);
				throw NEGATIVE_ID_EXCEPTION.createWithContext(selectorReader.getReader());
			}
		}, (selectorReader) -> {
			NumberRange.IntRange range = ((EntitySelectorReaderInterface) selectorReader).getIdRange();
			return range == null || range.isDummy();
		}, new LiteralText(I18N.translate("misc.idopt.desc")));
		
		putOption("side", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			String side = selectorReader.getReader().readUnquotedString();
			selectorReader.setSuggestionProvider((builder, consumer) -> {
				return CommandSource.suggestMatching(new String[] {"client", "server"}, builder);
			});
			switch(side) {
			case "client": 
				((EntitySelectorReaderInterface) selectorReader).setSide(NetworkSide.CLIENTBOUND);
				break;
			case "server": 
				((EntitySelectorReaderInterface) selectorReader).setSide(NetworkSide.SERVERBOUND);
				break;
			default: 
				selectorReader.getReader().setCursor(i);
				throw UNDEFINED_EXCEPTION.createWithContext(selectorReader.getReader(), side);
			}
		}, (selectorReader) -> {
			return !MessMod.isDedicatedEnv() && ((EntitySelectorReaderInterface) selectorReader).getSide() == null;
		}, new LiteralText(I18N.translate("misc.side.desc")));
		
		putOption("typeRegex", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			String regexStr = selectorReader.getReader().readQuotedString();
			selectorReader.setSuggestionProvider((builder, consumer) -> {
				return CommandSource.suggestMatching(new String[] {"\""}, builder);
			});
			try {
				((EntitySelectorReaderInterface) selectorReader).setTypeRegex(Pattern.compile(regexStr));
			} catch (PatternSyntaxException e) {
				selectorReader.getReader().setCursor(i);
				throw INVALID_REGEX_EXCEPTION.create(e);
			}
		}, (selectorReader) -> {
			return ((EntitySelectorReaderInterface) selectorReader).getTypeRegex() == null;
		}, new LiteralText(I18N.translate("misc.typeRegex.desc")));
		
		putOption("nameRegex", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			String regexStr = selectorReader.getReader().readQuotedString();
			selectorReader.setSuggestionProvider((builder, consumer) -> {
				return CommandSource.suggestMatching(new String[] {"\""}, builder);
			});
			try {
				((EntitySelectorReaderInterface) selectorReader).setNameRegex(Pattern.compile(regexStr));
			} catch (PatternSyntaxException e) {
				selectorReader.getReader().setCursor(i);
				throw INVALID_REGEX_EXCEPTION.create(e);
			}
		}, (selectorReader) -> {
			return ((EntitySelectorReaderInterface) selectorReader).getNameRegex() == null;
		}, new LiteralText(I18N.translate("misc.nameRegex.desc")));
		
		putOption("class", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			String regexStr = selectorReader.getReader().readQuotedString();
			selectorReader.setSuggestionProvider((builder, consumer) -> {
				return CommandSource.suggestMatching(new String[] {"\""}, builder);
			});
			try {
				((EntitySelectorReaderInterface) selectorReader).setClassRegex(Pattern.compile(regexStr));
			} catch (PatternSyntaxException e) {
				selectorReader.getReader().setCursor(i);
				throw INVALID_REGEX_EXCEPTION.create(e);
			}
		}, (selectorReader) -> {
			return ((EntitySelectorReaderInterface) selectorReader).getClassRegex() == null;
		}, new LiteralText(I18N.translate("misc.class.desc")));
		
		putOption("instanceof", (selectorReader) -> {
			int i = selectorReader.getReader().getCursor();
			String str = selectorReader.getReader().readQuotedString();
			selectorReader.setSuggestionProvider((builder, consumer) -> {
				return CommandSource.suggestMatching(new String[] {"\""}, builder);
			});
			String className = MessMod.INSTANCE.getMapping().srgClass(str.replace('/', '.'));
			try {
				Class<?> cl = Class.forName(className);
				((EntitySelectorReaderInterface) selectorReader).setInstanceofClass(cl);
			} catch (ClassNotFoundException e) {
				selectorReader.getReader().setCursor(i);
				throw NO_CLASS_EXCEPTION.create(className);
			}
		}, (selectorReader) -> {
			return ((EntitySelectorReaderInterface) selectorReader).getNameRegex() == null;
		}, new LiteralText(I18N.translate("misc.instanceof.desc")));
    }
}
