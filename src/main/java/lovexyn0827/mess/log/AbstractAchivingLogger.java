package lovexyn0827.mess.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import net.minecraft.server.MinecraftServer;

public abstract class AbstractAchivingLogger {
	public static final Logger LOGGER = LogManager.getLogger();
	private Path logPath;
	private long lastSessionStart;
	protected boolean hasCreatedAnyLog;
	protected final MinecraftServer server;

	public AbstractAchivingLogger(MinecraftServer server) {
		this.server = server;
		this.initialize(server);
	}

	public void initialize(MinecraftServer server) {
		this.lastSessionStart = System.currentTimeMillis();
		this.logPath = server.getSavePath(WorldSavePathMixin.create(this.getLogFolderName())).toAbsolutePath();
		if(!Files.exists(this.logPath)) {
			try {
				Files.createDirectory(this.logPath);
			} catch (IOException e) {
				LOGGER.fatal("Failed to create folder for entity logs!");
				e.printStackTrace();
				// XXX rethrow
			}
		}
	}

	protected abstract String getLogFolderName();

	public Path getLogPath() {
		return this.logPath;
	}

	public void archiveLogs() throws IOException {
		Path archiveDir = this.logPath.resolve("archived");
		if(!Files.exists(archiveDir)) {
			Files.createDirectory(archiveDir);
		}
		
		if(this.hasCreatedAnyLog) {
			String fn = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";
			Path archive = archiveDir.resolve(fn);
			try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive.toFile()))) {
				Files.walk(this.logPath, 1)
						.filter((f) -> f.getFileName().toString().endsWith(".csv"))
						.filter((f) -> f.toFile().lastModified() >= this.lastSessionStart)
						.forEach((f) -> {
							try {
								zos.putNextEntry(new ZipEntry(f.getFileName().toString()));
								zos.write(Files.readAllBytes(f));
								Files.delete(f);
							} catch (IOException e) {
								MessMod.LOGGER.warn("Failed to archive " + f.toString());
								e.printStackTrace();
							}
						});
				zos.finish();
			}
			
			LOGGER.info("Archived the logs to " + archive.toAbsolutePath().toString());
		}
	}

}