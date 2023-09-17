package lovexyn0827.mess;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Mainly used to determine if some mixins should be actually applied.
 * @author lovexyn0827
 * @date 2022/9/30
 */
public class MessModMixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String MESSMOD_MIXINS = "lovexyn0827.mess.mixins.";
	private static final File ADVANCED_MIXINS_CONFIGURATION = new File("advanced_mixins.prop");
	private static final Map<String, BooleanSupplier> CUSTOM_MINIX_REQUIREMENTS = new HashMap<>();
	private static final ImmutableSet<String> ADVANCED_MIXINS;
	private static final ImmutableSet<String> ACTIVIATED_ADVANCED_MIXINS;
	
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(ADVANCED_MIXINS.contains(mixinClassName) && !ACTIVIATED_ADVANCED_MIXINS.contains(mixinClassName)) {
			return false;
		}
		
		try {
			mixinClassName = mixinClassName.replace(MESSMOD_MIXINS, "");
			if(CUSTOM_MINIX_REQUIREMENTS.containsKey(mixinClassName)) {
				return CUSTOM_MINIX_REQUIREMENTS.get(mixinClassName).getAsBoolean();
			}
			
			return true;
		} catch (Exception e) {
			LOGGER.error("Failed to determine whether or not mixin {} should be applied!", mixinClassName);
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
	
	private static BooleanSupplier isModLoaded(String id, @Nullable String minVer, @Nullable String maxVer) {
		Optional<ModContainer> mayMod = FabricLoader.getInstance().getModContainer(id);
		SemanticVersion minSemiVer;
		SemanticVersion maxSemiVer;
		try {
			if(minVer != null) {
				minSemiVer = SemanticVersion.parse(minVer);
			} else {
				minSemiVer = null;
			}
			
			if(maxVer != null) {
				maxSemiVer = SemanticVersion.parse(maxVer);
			} else {
				maxSemiVer = null;
			}
		} catch (VersionParsingException e) {
			throw new IllegalArgumentException();
		}
		
		return () -> {
			if(mayMod.isPresent()) {
				Version ver = mayMod.get().getMetadata().getVersion();
				if(ver instanceof SemanticVersion) {
					SemanticVersion semVer = (SemanticVersion) ver;
					return (maxSemiVer == null ? true : maxSemiVer.compareTo(semVer) >= 0) 
							&& (minSemiVer == null ? true : minSemiVer.compareTo(semVer) <= 0);
				} else {
					return true;
				}
			} else {
				return true;
			}
		};
	}
	
	private static BooleanSupplier isModNotLoaded(String id, @Nullable String minVer, @Nullable String maxVer) {
		return () -> !isModLoaded(id, minVer, maxVer).getAsBoolean();
	}

	private static ImmutableSet<String> getActiviatedAdvancedMixins(ImmutableSet<String> advancedMixins) {
		try {
			Properties config = new Properties();
			if(!ADVANCED_MIXINS_CONFIGURATION.exists()) {
				ADVANCED_MIXINS_CONFIGURATION.createNewFile();
				advancedMixins.forEach((entry) -> {
					config.put(entry, "false");
				});
				
				try(FileWriter fw = new FileWriter(ADVANCED_MIXINS_CONFIGURATION)) {
					config.store(fw, "Advanced Mixins of MessMod");
				}
				
				return ImmutableSet.of();
			}
			
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			try(FileReader fr = new FileReader(ADVANCED_MIXINS_CONFIGURATION)) {
				config.load(fr);
				config.forEach((k, v) -> {
					if(Boolean.parseBoolean((String) v)) {
						builder.add((String) k);
					}
				});
			}
			
			return builder.build();
		} catch (Exception e) {
			LOGGER.fatal("Failed to load activation config of advanced mixins!");
			e.printStackTrace();
			return ImmutableSet.of();
		}
	}
	
	static {
		CUSTOM_MINIX_REQUIREMENTS.put("StructureBlockBlockEntityMixin", isModNotLoaded("carpet", "1.4.25", null));
		ADVANCED_MIXINS = ImmutableSet.<String>builder()
				.add("ServerChunkManagerMainThreadExecutorMixin")
				.build();
		ACTIVIATED_ADVANCED_MIXINS = getActiviatedAdvancedMixins(ADVANCED_MIXINS);
	}
}
