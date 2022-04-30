package lovexyn0827.mess.util.i18n;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

/**
 * Translation.
 * @author lovexyn0827
 * Date: April 10, 2022
 */
public class I18N {
	public static final Language EN_US;
	private static Language currentLanguage;
	
	public static String translate(String s) {
		return currentLanguage.translate(s);
	}
	
	public static boolean setLanguage(String name) {
		try {
			currentLanguage = new Language(name);
			return currentLanguage.vaildate();
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
