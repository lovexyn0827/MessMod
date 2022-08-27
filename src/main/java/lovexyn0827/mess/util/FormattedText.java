package lovexyn0827.mess.util;

import java.util.Locale;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.FormattingAccessor;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class FormattedText {
	private String content;
	private String format;
	private Object[] args;

	/**
	 * <table>
	 * 	<caption>Colors</caption>
	 * 	<tr><td>Flag</td><td>Color</td></tr>
	 * 	<tr><td>0</td><td>Black</td></tr>
	 * 	<tr><td>1</td><td>Dark blue</td></tr>
	 * 	<tr><td>2</td><td>Dark green</td></tr>
	 * 	<tr><td>3</td><td>Dark aqua</td></tr>
	 * 	<tr><td>4</td><td>Dark red</td></tr>
	 * 	<tr><td>5</td><td>Dark purple</td></tr>
	 * 	<tr><td>6</td><td>Gold</td></tr>
	 * 	<tr><td>7</td><td>Gray</td></tr>
	 * 	<tr><td>8</td><td>Dark gray</td></tr>
	 * 	<tr><td>9</td><td>Blue</td></tr>
	 * 	<tr><td>a</td><td>Green</td></tr>
	 * 	<tr><td>b</td><td>Aqua</td></tr>
	 * 	<tr><td>c</td><td>Red</td></tr>
	 * 	<tr><td>d</td><td>Light purple</td></tr>
	 * 	<tr><td>e</td><td>Yellow</td></tr>
	 * 	<tr><td>f</td><td>white</td></tr>
	 * </table>
	 * <table>
	 * 	<caption>Formats</caption>
	 * 	<tr><td>Flag</td><td>Formatting</td></tr>
	 * 	<tr><td>k</td><td>Obfuscated</td></tr>
	 * 	<tr><td>l</td><td>Bold</td></tr>
	 * 	<tr><td>m</td><td>Strike through</td></tr>
	 * 	<tr><td>n</td><td>Underline</td></tr>
	 * 	<tr><td>o</td><td>Italic</td></tr>
	 * 	<tr><td>r</td><td>Reset</td></tr>
	 * </table>	
	 * @param content The main content of the text.
	 * @param format Formatting flags
	 * @param args
	 */
	public FormattedText(String content, String format, boolean translate, Object ... args) {
		this.content = translate ? I18N.translate(content) : content;
		this.format = format;
		this.args = args;
	}
	
	/**
	 * Like {@link FormattedText#FormattedText(String, String, boolean, Object...)}, but the format will be translated by default.
	 */
	public FormattedText(String content, String format) {
		this.content = I18N.translate(content);
		this.format = format;
		this.args = new Object[0];
	}
	
	public MutableText asMutableText() {
		MutableText mt = new LiteralText(String.format(this.content, this.args));
		for(char c : this.format.toCharArray()) {
			Formatting f = byCode(c);
			if(f == null) {
				MessMod.LOGGER.warn("Unknown formatting flag " + c + ", ignoring it.");
				continue;
			}
			
			mt = mt.formatted(f);
		}
		
		return mt;
	}
	
	// Copied from Formatting.byCode()
	private static Formatting byCode(char code) {
		char c = Character.toString(code).toLowerCase(Locale.ROOT).charAt(0);
		Formatting[] var2 = Formatting.values();
		int var3 = var2.length;
		for(int var4 = 0; var4 < var3; ++var4) {
			Formatting formatting = var2[var4];
			if (((FormattingAccessor)(Object) formatting).getCode() == c) {
				return formatting;
			}
		}

		return null;
	}
}
