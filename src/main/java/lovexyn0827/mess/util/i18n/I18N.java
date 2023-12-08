package lovexyn0827.mess.util.i18n;

import com.google.common.collect.ImmutableSet;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

/**
 * Translation.
 * @author lovexyn0827
 * Date: April 10, 2022
 */
public class I18N {
	/**
	 * Remember to add the name of the added language to make it present in command suggestions
	 */
	public static final ImmutableSet<String> SUPPORTED_LANGUAGES = ImmutableSet.<String>builder().add("zh_cn", "en_us").build();
	public static final Language EN_US;
	private static Language currentLanguage;
	
	public static String translate(String translationKey) {
		return currentLanguage.translate(translationKey);
	}

	public static String translate(String translationKey, Object ... args) {
		return String.format(translate(translationKey), args);
	}
	
	@SuppressWarnings("resource")
	public static boolean setLanguage(String name, boolean forceLoad) {
		if(name == null || "-FOLLOW_SYSTEM_SETTINGS-".equals(name)) {
			forceLoad = true;
			if (!MessMod.isDedicatedEnv() && MinecraftClient.getInstance().options != null) {
				String sysLang = MinecraftClient.getInstance().options.language;
				if (I18N.SUPPORTED_LANGUAGES.contains(sysLang)) {
					name = sysLang;
				} else {
					name = "en_us";
				} 
			} else {
				name = "en_us";
			} 
		}
		
		try {
			Language lang = new Language(name);
			if(forceLoad || lang.vaildate()) {
				currentLanguage = lang;
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static Language getCurrentLanguage() {
		return currentLanguage;
	}
	
	static {
		try {
			EN_US = new Language("en_us");
			currentLanguage = EN_US;
		} catch (Exception e) {
			throw new CrashException(new CrashReport("Couldn't load the default translation.", e));
		}
	}
}
