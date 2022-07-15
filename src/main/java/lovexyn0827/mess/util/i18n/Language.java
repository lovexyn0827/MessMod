package lovexyn0827.mess.util.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.InvaildOptionException;
import lovexyn0827.mess.options.OptionParser;
import net.minecraft.server.command.ServerCommandSource;

/**
 * A language definition. Only the in-game contents will be translated.
 */
public class Language {
	private final String readableName;
	private final Map<String, String> translations = Maps.newHashMap();
	private final String id;
	
	/**
	 * Create a new @{code Language} instance using the definition in assets/lang/@{code name}.json
	 * @param id Can be something like en_us, zh_cn, and so on. 
	 * @throws IOException When
	 */
	public Language(String id) throws Exception {
		this.id = id;
		try(Reader r = new InputStreamReader(
				Language.class.getResourceAsStream("/assets/lang/" + id + ".json"))) {
			JsonObject def = new JsonParser().parse(r).getAsJsonObject();
			this.readableName = def.get("readableName").getAsString();
			def.getAsJsonObject("translations")
					.entrySet()
					.forEach((e) -> this.translations.put(e.getKey(), e.getValue().getAsString()));
		} catch (Exception e) {
			MessMod.LOGGER.error("Failed to load the definition of language " + id);
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @param key The translation key
	 * @return The translated content or the translation key if the key is undefined.
	 */
	public String translate(String key) {
		if(!this.translations.containsKey(key)) {
			// XXX Should this warning be removed?
			//MessMod.LOGGER.warn("Couldn't find the translated text for " + key + " in " + this.readableName + "!");
			return key; 
		}
		
		return this.translations.get(key);
	}
	
	public String getName() {
		return this.readableName;
	}
	
	/**
	 * Compare translation keys against en_us.json, to find absence and redundancy in the keys.
	 * @return {@code true} if all the translation keys here is also in en_us.json, and all keys in en_us.json is also here, {@code false} otherwise.
	 */
	public boolean vaildate() {
		if("en_us".equals(this.id)) {
			return true;
		} else {
			Set<String> here = this.translations.keySet();
			Set<String> en = I18N.EN_US.translations.keySet();
			if(here.containsAll(en) && en.containsAll(here)) {
				return true;
			} else {
				en.stream().filter((key) -> !here.contains(key)).forEach((key) -> MessMod.LOGGER.warn("Absence: " + key));
				here.stream().filter((key) -> !en.contains(key)).forEach((key) -> MessMod.LOGGER.warn("Redunancy: " + key));
				return false;
			}
		}
	}
	
	public static class Parser implements OptionParser<String> {

		@Override
		public String tryParse(String str) throws InvaildOptionException {
			// TODO Move application steps to where it should be.
			boolean forceLoad = str.endsWith("_FORCELOAD");
			if(I18N.setLanguage(str.replace("_FORCELOAD", ""), forceLoad)) {
				return str;
			} else {
				// Needn't be translated
				throw new InvaildOptionException("The language is unsupported currently or incomplete.");
			}
		}

		@Override
		public String serialize(String val) {
			return val;
		}
		
		@Override
		public SuggestionProvider<ServerCommandSource> createSuggestions() {
			return (ct, b) -> {
				b.suggest("-FOLLOW_SYSTEM_SETTINGS-");
				I18N.SUPPORTED_LANGUAGES.forEach((l) -> {
					b.suggest(l);
					b.suggest(l + "_FORCELOAD");
				});
				return b.buildFuture();
			};
		}
	}
}
