package lovexyn0827.mess.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.OptionParser;
import lovexyn0827.mess.util.i18n.I18N;

public class DocumentGenerator {
	public static void optionDoc() {
		boolean chinese = I18N.getCurrentLanguage().getId().contains("zh");
		try(PrintStream os = new PrintStream(new FileOutputStream(new File("README_Options" + (chinese ? "_zh_cn" : "") + System.currentTimeMillis() + ".md")))){
			OptionManager.OPTIONS.forEach((name, opt) -> {
				String desc = opt.getDescription();
				String values = OptionParser.of(name).getAvailableValues(chinese);
				os.append("##### `" + name + "`\n\n");
				os.append(desc + "\n\n");
				os.append((chinese ? "可能取值：" : "Available values: ") + values + "\n\n");
				os.append((chinese ? "默认值：`" : "Default value: `") + opt.getDefaultValue() + "`\n\n");
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
