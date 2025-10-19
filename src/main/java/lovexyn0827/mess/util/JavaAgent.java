package lovexyn0827.mess.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class JavaAgent {
	private final Path path;
	
	private JavaAgent(Path path) {
		this.path = path;
	}
	
	public static JavaAgent download(URL url) {
		try (InputStream in = url.openConnection().getInputStream()) {
			Path target = Paths.get(String.format("MessModAgent%d.jar", System.currentTimeMillis()));
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			return new JavaAgent(target);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JavaAgent load(Path path) {
		return new JavaAgent(path);
	}
	
	public boolean attach() {
		try {
			Class<?> phClass = Class.forName("java.lang.ProcessHandle");
			Object ph = phClass.getMethod("current").invoke(null);
			long pid = (long) phClass.getMethod("pid").invoke(ph);
			return this.attach(pid);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean attach(long pid) {
		try {
			Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
			Object vmInstance = vmClass.getMethod("attach", String.class).invoke(vmClass, Long.toString(pid));
			vmClass.getMethod("loadAgent", String.class).invoke(vmInstance, this.path.toAbsolutePath().toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
