package lovexyn0827.mess.util.deobfuscating;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import lovexyn0827.mess.MessMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

// TODO Support for non-tiny mappings.
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
	
	/**
	 * @return The required mapping or a dummy mapping if loading failed.
	 */
	@NotNull
	public Mapping tryLoadMapping() {
		Mapping dummy = new DummyMapping();
		
		try {
			Class.forName("net.minecraft.entity.Entity$823");	// TODO Remove $827 if needed
			LOGGER.info("The Minecraft has probably been deobfuscated, the mapping won't be loaded");
			return new DummyMapping();
		} catch (ClassNotFoundException e) {
			File mappingFolder = new File("mappings");
			if(!mappingFolder.exists()) {
				mappingFolder.mkdir();
			}
			
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
		@SuppressWarnings("deprecation")
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
			String mcVer = SharedConstants.getGameVersion().getName();
			LOGGER.info("Trying to download the lastest yarn mapping from Maven...");
			long start = Util.getMeasuringTimeMs();
			URL listUrl = new URL("https://maven.fabricmc.net/net/fabricmc/yarn/maven-metadata.xml");
			NavigableSet<String> foundMappings = new TreeSet<>();
			try (InputStream is = listUrl.openStream()) {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
				NodeList versions = doc.getElementsByTagName("version");
				for(int i = 0; i < versions.getLength(); i++) {
					String ver = versions.item(i).getTextContent();
					if(ver.replaceAll("\\+.+", "").equals(mcVer)) {
						foundMappings.add(ver.replace("+", "%2B"));
					};
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.warn("Failed to parse the mapping version list!");
				return false;
			}
			
			if(foundMappings.isEmpty()) {
				LOGGER.warn("No corresponding mapping was found.");
				return false;
			} else {
				String latest = foundMappings.last();
				URL url = new URL("https://maven.fabricmc.net/net/fabricmc/yarn/" + latest + "/yarn-" + latest + "-v2.jar");
				//URL url = new URL("file:///M:/SOURCE%20CODE/yarn-1.16.4+build.9-v2.jar");
				Path temp = Files.createTempFile("yarn-" + mcVer, ".jar");
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
			}
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
