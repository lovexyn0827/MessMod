package lovexyn0827.mess.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import lovexyn0827.mess.options.Option;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.i18n.I18N;

public class DocumentGenerator {
	@SuppressWarnings("deprecation")
	public static void optionDoc() {
		boolean chinese = I18N.getCurrentLanguage().getId().contains("zh");
		try(PrintStream os = new PrintStream(new FileOutputStream(new File("README_Options" + (chinese ? "_zh_cn" : "") + ".md")))){
			OptionManager.OPTIONS.forEach((f) -> {
				try {
					Option o = f.getAnnotation(Option.class);
					String desc = OptionManager.getDescription(f.getName());
					String values = o.parserClass().newInstance().getAvailableValues(chinese);
					os.append("##### `" + f.getName() + "`\n\n");
					os.append(desc + "\n\n");
					os.append((chinese ? "可能取值：" : "Available values: ") + values + "\n\n");
					os.append((chinese ? "默认值：`" : "Default value: `") + o.defaultValue() + "`\n\n");
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
