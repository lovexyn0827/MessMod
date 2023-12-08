package lovexyn0827.mess.util.i18n;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.InvalidOptionException;
import lovexyn0827.mess.options.OptionParser;
import net.fabricmc.loader.api.FabricLoader;

/**
 * A language definition. Only the in-game contents will be translated.
 */
public class Language {
	public static final String FORCELOAD_SUFFIX = "_FORCELOAD";
	private final String readableName;
	private final Map<String, String> translations = Maps.newHashMap();
	private final String id;
	
	/**
	 * Create a new @{code Language} instance using the definition in assets/lang/@{code name}.json
	 * @param id Can be something like en_us, zh_cn, and so on. 
	 * @throws IOException When
	 */
	@SuppressWarnings("deprecation")
	public Language(String id) throws Exception {
		this.id = id;
		Path langFile = FabricLoader.getInstance().getModContainer("messmod")
				.get().getRootPath().resolve("assets/lang/" + id + ".json");
		try {
			JsonObject def = new JsonParser()
					.parse(new String(Files.readAllBytes(langFile), Charset.forName("GBK")))
					.getAsJsonObject();
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
			return key; 
		}
		
		return this.translations.get(key);
	}
	
	public String getName() {
		return this.readableName;
	}
	
	/**
	 * Compare translation keys against en_us.json, to find absence and redundancy in the keys.
	 * @return {@code true} if all the translation keys here is also in en_us.json, 
	 * and all keys in en_us.json is also here, {@code false} otherwise.
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
				en.stream()
						.filter((key) -> !here.contains(key))
						.forEach((key) -> MessMod.LOGGER.warn("Absence: " + key));
				here.stream()
						.filter((key) -> !en.contains(key))
						.forEach((key) -> MessMod.LOGGER.warn("Redunancy: " + key));
				return false;
			}
		}
	}

	public boolean containsKey(String mayKey) {
		return this.translations.containsKey(mayKey);
	}
	
	public static class Parser implements OptionParser<String> {
		@Override
		public String tryParse(String str) throws InvalidOptionException {
			if("-FOLLOW_SYSTEM_SETTINGS-".equals(str) || 
					I18N.SUPPORTED_LANGUAGES.contains(str.replace(FORCELOAD_SUFFIX, ""))) {
				return str;
			} else {
				throw new InvalidOptionException("opt.err.nodef", str);
			}
		}

		@Override
		public String serialize(String val) {
			return val;
		}
		
		@Override
		public Set<String> createSuggestions() {
			Set<String> langs = new HashSet<>();
			langs.add("-FOLLOW_SYSTEM_SETTINGS-");
			I18N.SUPPORTED_LANGUAGES.forEach((l) -> {
				langs.add(l);
				langs.add(l + FORCELOAD_SUFFIX);
			});
			return langs;
		}
	}
}
