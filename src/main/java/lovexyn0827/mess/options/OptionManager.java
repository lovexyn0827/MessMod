package lovexyn0827.mess.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.BlockInfoRenderer.ShapeType;
import lovexyn0827.mess.rendering.hud.AlignMode;
import lovexyn0827.mess.util.i18n.Language;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

/**
 * <p>There are three layers in the option storage, that is, hard-coded global default values, global option value, and the save-local values.</p>
 * All fields and methods here are declared static in order to speed up option reading.
 * @author lovexyn0827
 * Date: April 2, 2022
 */
public class OptionManager{
	// TODO Move the descriptions to the language file
	private static final File GLOBAL_OPTION_FILE = new File(FabricLoader.getInstance().getGameDir().toString() + "/mcwmem.prop");
	private static final Properties GLOBAL_OPTION_SERIALIZER = new Properties();
	public static final List<Field> OPTIONS = Stream.of(OptionManager.class.getFields())
			.filter((f) -> f.isAnnotationPresent(Option.class))
			.sorted((a, b) -> Comparator.<String>naturalOrder().compare(a.getName(), b.getName()))
			.collect(Collectors.toList());
	private static File localOptionFile;
	private static Properties localOptionSerializer;
	
	/**
	 * Actions taken right after an option is set to a given value.
	 */
	public static final Map<String, BiConsumer<String, CommandContext<ServerCommandSource>>> CUSTOM_APPLICATION_BEHAVIORS = Maps.newHashMap();
	
	@Option(description = "TNTs could be killed by attacking.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean attackableTnt;
	
	// TODO Write to the document and implement it.
	@Option(description = "What will the block information renderers do in ticks frozen by the Carpet.", 
			defaultValue = "NORMALLY", 
			experimental = true, 
			parserClass = BlockInfoRenderer.FrozenUpdateMode.Parser.class)
	public static BlockInfoRenderer.FrozenUpdateMode blockInfoRendererUpdateInFrozenTicks;
	
	@Option(description = "Specify the type of block shape rendered when `renderBlockShape` is enabled. "
			+ "The COLLIDER shape is the  shape used to do calculations about collisions, while the OUTLINE "
			+ "shape is the shape used to determine which block the player is looking at.", 
			defaultValue = "COLLISION", 
			parserClass = ShapeType.Parser.class)
	public static ShapeType blockShapeToBeRendered;

	@Option(description = "Whether or not execution of commands defined by this mod require OP permission.", 
			defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean commandExecutionRequirment;
	
	@Option(description = "Set the speed which the player is flying upwards at in the creative mode.", 
			defaultValue = "0.05", 
			suggestions = {"0.05", "0.10"}, 
			parserClass = FloatParser.Positive.class)
	public static float creativeUpwardsSpeed;
	
	@Option(description = "Prevent debug sticks change blocks to a invalid state. By now, the option "
			+ "**doesn't work** in some cases, like changing the `shape` property of a rail can still "
			+ "turn the rail in to an illegal state and get broken. ", 
			defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean debugStickSkipsInvaildState;
	
	@Option(description = "Disable the calculation of explosion exposure to reduce the lag caused by stacked "
			+ "TNT explosions. This will also mean that blocks cannot prevent entities from be influenced by "
			+ "explosions.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableExplosionExposureCalculation;
	
	@Option(description = "Remove the random speed of projectiles. It could be used in testing, but don't "
			+ "forget to disable it if not needed.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableProjectileRandomness;
	
	@Option(description = "Item tools, which makes bone and bricks powerful. Requires carpet-fabric.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean enabledTools;
	
	@Option(description = "When the player uses ender eyes, teleport it to where it looks at.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean endEyeTeleport;
	
	@Option(description = "Send how entities are affected by explosions. This feature may not work properly if "
			+ "the Lithium is loaded.", 
			defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean entityExplosionInfluence;
	
	@Option(description = "Explosion ray (used to calculate the exposure of entities) renderer.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean entityExplosionRaysVisiblity;
	
	@Option(description = "The number of ticks the rendered rays remains. ", 
			defaultValue = "300", 
			parserClass = IntegerParser.class)
	public static int entityExplosionRaysLifetime;
	
	@Option(description = "Archive the entity log produced within a single session automatically.", 
			defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean entityLogAutoArchiving;
	
	// TODO
	@Option(description = "In the vanilla getEntities() method, only entities which are in subchunks whose Cheshev distance"
			+ "to the given AABB is smaller than 2 blocks is could be seen. Usually it doesn't matter, but when height of some of the "
			+ "entities is greater than 2 blocks or the width is greater than 4 blocks, it can cause some issues, especally when"
			+ "the entity is close to the boundary of subchunks. Change it to a higher value may fix some bugs about interaction "
			+ "between entities and something else.", 
			defaultValue = "2.0", 
			experimental = true, 
			parserClass = FloatParser.Positive.class)
	public static float getEntityRangeExpansion;
	
	@Option(description = "Move the HUDs to the given location.  ", 
			defaultValue = "TOP_RIGHT", 
			parserClass = AlignMode.Parser.class)
	public static AlignMode hudAlignMode;
	
	@Option(description = "Set the size of the text in the HUDs.", 
			defaultValue = "1.0", 
			parserClass = FloatParser.Positive.class)
	public static float hudTextSize;
	
	@Option(description = "The main language of the Mod", 
			defaultValue = "en_us", 
			parserClass = Language.Parser.class)
	public static String language;
	
	@Option(description = "Set the maximum number of ticks can be processed within a single frame when the "
			+ "FPS is lower than 20, setting it to a low value may fix the bug which makes players cannot "
			+ "toggle the flying state when the FPS is too low. ", 
			defaultValue = "10", 
			parserClass = IntegerParser.Positive.class)
	public static int maxClientTicksPerFrame;
	
	@Option(description = "Set the maximum range of teleporting with endEyeTeleport.", 
			defaultValue = "180", 
			parserClass = FloatParser.Positive.class)
	public static float maxEndEyeTpRadius;
	
	@Option(description = "/kill kill mobs by removes them directly instead of damaging them.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean mobFastKill;
	
	@Option(description = "Allow projectiles to load chunks in their processing, maybe helpful in testing "
			+ "pearl canons.  Note that if a projectile flies at a extremely high speed when the option is "
			+ "set to true.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean projectileChunkLoading;
	
	@Option(description = "Set the radius of entity processing chunks created by projectileChunkLoading.", 
			defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class)
	public static int projectileChunkLoadingRange;
	
	@Option(description = "Projectiles load the chunks they are in permanently when projectileChunkLoading is enabled.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean projectileChunkLoadingPermanence;
	
	// TODO
	@Option(description = "Prevent the shape of rails from being changed by sorrounding blocks.", 
			defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean railNoAutoConnection;
	
	@Option(description = "Enable or disable block boundary box renderer.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderBlockShape;
	
	@Option(description = "Enable or disable the renderer of outlines, heights, and vectors of flowing directions "
			+ "of target fluids.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderFluidShape;
	
	@Option(description = "Display the output level of repeaters and comparators the player looks at.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderRedstoneGateInfo;
	
	@Option(description = "Enable or disable the server-side hitbox renderer.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean serverSyncedBox;
	
	@Option(description = "Make the location of huds more stable when the length of lines change frequently.", 
			defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean stableHudLocation;
	
	// TODO
	@Option(description = "Treat accessing paths strictly, to make them more relyable. Disable it may make accessing "
			+ "processes more likely to fail in varible environments.", 
			defaultValue = "true", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean strictAccessingPathParsing;
	
	@Option(description = "wlujkgfdhlqcmyfdhj...", 
			defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean superSuperSecretSetting;
	
	@Option(description = "Allow or disallow TNT entities to load chunks in their processing, maybe helpful "
			+ "in making some kind of TNT canons. Note that if an TNT entity flies at a extremely high speed "
			+ "when the option is set to true.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean tntChunkLoading;
	
	@Option(description = "TNT entities load the chunks they are in permanently when tntChunkLoading is enabled.", 
			defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean tntChunkLoadingPermanence;
	
	@Option(description = "Set the radius of entity processing chunks created by tntChunkLoading.", 
			defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class)
	public static int tntChunkLoadingRange;
	
	/**
	 * Refresh the save-local option storage
	 */
	public static synchronized void reload() {
		if(localOptionFile.exists()) {
			try (FileInputStream in = new FileInputStream(localOptionFile)) {
				localOptionSerializer.load(in);
			} catch (IOException e) {
				MessMod.LOGGER.fatal("Failed to open mcwmem.prop, the Minecraft may crash later.");
				e.printStackTrace();
			}
		} else {
			MessMod.LOGGER.info("Couldn't find mcwmem.prop, creating a new one.");
			writeDefault();
		}
		
		for(Field f : OPTIONS) {
			String name = f.getName();
			try {
				Option o = f.getAnnotation(Option.class);
				OptionParser<?> parser = o.parserClass().newInstance();
				try {
					localOptionSerializer.computeIfAbsent(name, 
							(t) -> GLOBAL_OPTION_SERIALIZER.computeIfAbsent(name, (key) -> o.defaultValue()));
					f.set(null, parser.tryParse((String) localOptionSerializer.get(name)));
					saveGlobal();
				} catch (InvaildOptionException e) {
					MessMod.LOGGER.warn("The value of option {} is invaild, restoring it to the default value: {}", name, e.getMessage());
					String dV;
					if(isVaild(parser, (String) GLOBAL_OPTION_SERIALIZER.get(name))) {
						dV = GLOBAL_OPTION_SERIALIZER.getProperty(name);
						localOptionSerializer.put(name, dV);
					} else {
						dV = o.defaultValue();
						GLOBAL_OPTION_SERIALIZER.put(name, dV);
						localOptionSerializer.put(name, dV);
					}
					
					try {
						f.set(null, parser.tryParse(dV));
					} catch (IllegalArgumentException | IllegalAccessException | InvaildOptionException e1) {
						e1.printStackTrace();
					}
				}
			} catch (SecurityException | IllegalAccessException | InstantiationException e1) {
				e1.printStackTrace();
			}
		}
		
		if(MessMod.LOGGER.isDebugEnabled()) {
			MessMod.LOGGER.debug("Loaded {} MessMod config from {}", OPTIONS.size(), localOptionFile.getAbsolutePath());
			OPTIONS.stream()
			.map((f) -> {
				return f.getName() + ": " + getString(f);
			})
			.forEach(MessMod.LOGGER::debug);
		}
	}

	// Only calls from <clinit> is permitted
	private static void loadGlobal() {
		if(GLOBAL_OPTION_FILE.exists()) {
			try (FileInputStream in = new FileInputStream(GLOBAL_OPTION_FILE)) {
				GLOBAL_OPTION_SERIALIZER.load(in);
			} catch (IOException e) {
				MessMod.LOGGER.fatal("Failed to open mcwmem.prop, the Minecraft may crash later.");
				e.printStackTrace();
			}
		} else {
			MessMod.LOGGER.info("Couldn't find mcwmem.prop, creating a new one.");
			writeDefault();
		}
		
		for(Field f : OPTIONS) {
			String name = f.getName();
			try {
				Option o = f.getAnnotation(Option.class);
				OptionParser<?> parser = o.parserClass().newInstance();
				try {
					GLOBAL_OPTION_SERIALIZER.computeIfAbsent(name, (k) -> o.defaultValue());
					f.set(null, parser.tryParse((String) GLOBAL_OPTION_SERIALIZER.get(name)));
					saveGlobal();
				} catch (InvaildOptionException e) {
					MessMod.LOGGER.warn("The value of option {} is invaild, restoring it to the default value: {}", name, e.getMessage());
					String dV = o.defaultValue();
					GLOBAL_OPTION_SERIALIZER.put(name, dV);
					try {
						f.set(null, parser.tryParse(dV));
					} catch (IllegalArgumentException | IllegalAccessException | InvaildOptionException e1) {
						e1.printStackTrace();
					}
				}
			} catch (SecurityException | IllegalAccessException | InstantiationException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private static boolean isVaild(OptionParser<?> parser, String val) {
		try {
			return (parser.tryParse(val) != null);
		} catch (InvaildOptionException e) {
			return false;
		}
	}
	
	private static void writeDefault() {
		OPTIONS.forEach((f) -> GLOBAL_OPTION_SERIALIZER.put(f.getName(), f.getAnnotation(Option.class).defaultValue()));
		saveGlobal();
	}

	private static void writeDefaultLocally() {
		OPTIONS.forEach((f) -> localOptionSerializer.put(f.getName(), 
				GLOBAL_OPTION_SERIALIZER.computeIfAbsent(f.getName(), (key) -> f.getAnnotation(Option.class).defaultValue())));
		save();
	}

	public static synchronized void save() {
		try (FileOutputStream in = new FileOutputStream(localOptionFile)) {
			localOptionSerializer.store(in,"MessMod Options");
		} catch (IOException e) {
			LogManager.getLogger().fatal("Failed to write mcwmem.prop!");
			e.printStackTrace();
		}
	}

	private static void saveGlobal() {
		try (FileOutputStream in = new FileOutputStream(GLOBAL_OPTION_FILE)) {
			GLOBAL_OPTION_SERIALIZER.store(in,"MessMod Options");
		} catch (IOException e) {
			LogManager.getLogger().fatal("Failed to write mcwmem.prop!");
			e.printStackTrace();
		}
	}

	public static void set(Field f, Object obj) {
		try {
			f.set(null, obj);
			localOptionSerializer.put(f.getName(), f.getAnnotation(Option.class).parserClass()
					.newInstance().serializeObj(obj));
			save();
		} catch (IllegalArgumentException e) {
			MessMod.LOGGER.fatal("Couldn't set the value of option {}, that shouldn't happed!", f.getName());
			e.printStackTrace();
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
	}

	public static void setGolbal(Field f, Object obj) {
		try {
			GLOBAL_OPTION_SERIALIZER.put(f.getName(), f.getAnnotation(Option.class).parserClass()
					.newInstance().serializeObj(obj));
			saveGlobal();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		set(f, obj);
	}

	public static String getString(Field f) {
		return localOptionSerializer.get(f.getName()).toString();
	}
	
	public static String getGolbalString(Field f) {
		return GLOBAL_OPTION_SERIALIZER.get(f.getName()).toString();
	}

	private static void registerCustomApplicationBehavior(String name, 
			@Nullable BiConsumer<String, CommandContext<ServerCommandSource>> behavior) {
		CUSTOM_APPLICATION_BEHAVIORS.put(name, behavior);
	}
	
	/**
	 * Update the 
	 * @param ms The current MinecraftServer, or null if closed.
	 */
	public static void updateServer(@Nullable MinecraftServer ms) {
		if(ms != null) {
			localOptionSerializer = new Properties();
			Path p = ms.getSavePath(WorldSavePathMixin.create("mcwmem.prop"));
			localOptionFile = p.toFile();
			if(!Files.exists(p)) {
				try {
					Files.createFile(p);
					writeDefaultLocally();
				} catch (IOException e) {
					throw new CrashException(new CrashReport("Failed to create config file for MessMod.", e));
				}
			}
			
			reload();
		} else {
			save();
			localOptionSerializer = null;
			localOptionFile = null;
		}
	}
	
	static{
		registerCustomApplicationBehavior("enabledTools", (val, ct) -> {
			if(!FabricLoader.getInstance().isModLoaded("carpet")) {
				CommandUtil.error(ct, "Please install the carpet mod!");
			}
			
			try {
				if(new BooleanParser().tryParse(val)) {
					CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script load tool");
				}else {
					CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script unload tool");
				}
			} catch (InvaildOptionException e) {
				e.printStackTrace();
			}
		});
		registerCustomApplicationBehavior("entityExplosionInfluence", (val, ct) -> {
			if(FabricLoader.getInstance().isModLoaded("lithium")) {
				CommandUtil.error(ct, "Warning: This feature is not compatible with lithium. Maybe it won't work properly");
			}
		});
		registerCustomApplicationBehavior("blockInfoRendererUpdateInFrozenTicks", (val, ct) -> {
			if(!FabricLoader.getInstance().isModLoaded("carpet")) {
				CommandUtil.error(ct, "Please install the carpet mod!");
			}
		});
		loadGlobal();
	}
}
