package mc.lovexyn0827.mcwmem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;

import net.fabricmc.loader.api.FabricLoader;

public class Options extends Properties{
	private static final long serialVersionUID = 2005082720050104L;
	private static final File OPTION_FILE = new File(FabricLoader.getInstance().getGameDir().toString()+"mcwmem.prop");
	private final Options defaults;
	
	public Options(boolean buildDefault) {
		super();
		if(!buildDefault) {
			this.defaults = new Options(true);
			this.defaults.put("alignMode", "topRight");
			this.load();
		}else {
			this.defaults = null;
		}
	}
	
	public void load() {
		try (FileInputStream in =new FileInputStream(OPTION_FILE)) {
			load(in);
		} catch (IOException e) {
			if(e instanceof FileNotFoundException) {
				writeDefault();
				load();
				return;
			}
			LogManager.getLogger().fatal("Failed to open mcwmem.prop");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void save() {
		try (FileOutputStream in =new FileOutputStream(OPTION_FILE)) {
			store(in,"MCWMEMod Options");
		} catch (IOException e) {
			LogManager.getLogger().fatal("Failed to write mcwmem.prop");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void writeDefault() {
		this.defaults.save();
	}
}