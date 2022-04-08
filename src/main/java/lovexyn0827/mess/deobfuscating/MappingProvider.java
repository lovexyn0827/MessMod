package lovexyn0827.mess.deobfuscating;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import lovexyn0827.mess.MessMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

// TODO Support for non-Yarn mappings.
/**
 * The mapping loader
 * @author lovexyn0827
 * Date: April 7, 2022
 */
public class MappingProvider {
	public static final Logger LOGGER = MessMod.LOGGER;
	private final Source source;
	
	public MappingProvider() {
		this.source = Source.YARN;
	}
	
	@NotNull
	/**
	 * @return The required mapping or a dummy mapping if loading failed.
	 */
	public Mapping tryLoadMapping() {
		Mapping dummy = new DummyMapping();
		
		try {
			Class.forName("net.minecraft.entity.Entity");	// TODO Remove $827 if needed
			LOGGER.info("The Minecraft has probably been deobfuscated, the mapping won't be loaded");
			return new DummyMapping();
		} catch (ClassNotFoundException e) {
			File mappingFile = new File(FabricLoader.getInstance().getGameDir().toString() + "/mappings/" + 
					SharedConstants.getGameVersion().getName() + ".tiny");
			try {
				if(mappingFile.exists()) {
					LOGGER.info("Found corresponding Tiny mapping, trying to load it...");
					return new TinyMapping(mappingFile);
				} else if(this.source == Source.YARN && FabricLoader.getInstance().isModLoaded("carpet-tis-addition")) {
					return tryLoadMappingFromTisAddition().orElse(dummy);
				} else if(this.source == Source.YARN && tryDownloadYarnMapping(mappingFile.toPath())) {
					LOGGER.info("Downloaded the mapping successfully, loading it...");
					return new TinyMapping(mappingFile);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			LOGGER.error("The mapping couldn't be downloaded, it is recommended to try to download the mapping manually. "
					+ "Deobfuscating will be disabled in this running.");
			LOGGER.error("The mapping should be downloaded to " + mappingFile.getAbsolutePath());
			return dummy;
		}
	}
	
	private static Optional<Mapping> tryLoadMappingFromTisAddition() throws IOException {
		LOGGER.info("Trying to load mapping bundled in Carpet TIS Addition...");
		Path path = Files.walk(FabricLoader.getInstance().getModContainer("carpet-tis-addition").get().getPath("assets/carpettisaddition"))
				.filter((f) -> f.getFileName().toString().endsWith(".tiny"))
				.findFirst()
				.orElse(null);
		if(path != null) {
			LOGGER.info("The mapping was found, loading it...");
			return Optional.of(new TinyMapping(Files.newBufferedReader(path)));
		}
		
		return Optional.empty();
	}
	
	private static boolean tryDownloadYarnMapping(Path to) {
		try {
			LOGGER.info("Trying to download the lastest yarn mapping from Maven...");
			long start = Util.getMeasuringTimeMs();
			URL url = new URL("https://maven.fabricmc.net/net/fabricmc/yarn/1.16.4%2Bbuild.9/yarn-1.16.4%2Bbuild.9-v2.jar");
			//URL url = new URL("file:///M:/SOURCE%20CODE/yarn-1.16.4+build.9-v2.jar");
			Path temp = Files.createTempFile("yarn-1.16.4", ".jar");
			InputStream is = url.openStream();
			Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
			is.close();
			try(ZipFile zf = new ZipFile(temp.toFile())) {
				Files.copy(zf.getInputStream(zf.getEntry("mappings/mappings.tiny")), to, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.warn("Failed to download the mapping: " + e);
				e.printStackTrace();
				return false;
			}
			
			long end = Util.getMeasuringTimeMs();
			LOGGER.info("Downloaded the mapping in roughly " + (end - start) + "ms.");
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			LOGGER.warn("Failed to download the mapping: " + e);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Currently unused actually, but may be used in future versions.
	 * @author lovexyn0827
	 * Date: April 7,2022
	 */
	public enum Source {
		YARN, 
		OFFICAL, 
		MCP
	}
}
