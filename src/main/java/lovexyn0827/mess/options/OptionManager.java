package lovexyn0827.mess.options;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.export.SaveComponent;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.options.RangeParser.ChunkStatusRange.ChunkStatusSorter;
import lovexyn0827.mess.rendering.BlockInfoRenderer.ShapeType;
import lovexyn0827.mess.rendering.FrozenUpdateMode;
import lovexyn0827.mess.rendering.hud.AlignMode;
import lovexyn0827.mess.util.PulseRecorder;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.blame.BlamingMode;
import lovexyn0827.mess.util.blame.Confidence;
import lovexyn0827.mess.util.i18n.I18N;
import lovexyn0827.mess.util.i18n.Language;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;

/**
 * <p>There are three layers in the option storage, that is, hard-coded global default values, global option value, and the save-local values.</p>
 * All fields and methods here are declared static in order to speed up option reading.
 * @author lovexyn0827
 * Date: April 2, 2022
 */
public class OptionManager{
	public static final SortedMap<String, OptionWrapper> OPTIONS = Stream.of(OptionManager.class.getFields())
			.filter((f) -> f.isAnnotationPresent(Option.class))
			.sorted((a, b) -> Comparator.<String>naturalOrder().compare(a.getName(), b.getName()))
			.collect(TreeMap::new, (map, f) -> map.put(f.getName(), new OptionWrapper(f)), Map::putAll);
	private static OptionSet activeOptionSet;
	
	/**
	 * Actions taken right after an option is set to a given value.
	 */
	static final Map<String, CustomOptionApplicator> CUSTOM_APPLICATION_BEHAVIORS = Maps.newHashMap();
	static final Map<String, CustomOptionValidator> CUSTOM_OPTION_VALIDATORS = Maps.newHashMap();

	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = Label.MESSMOD)
	public static boolean accessingPathDynamicAutoCompletion;
	
	@Option(defaultValue = "STANDARD", 
			parserClass = AccessingPath.InitializationStrategy.Parser.class, 
			label = Label.MESSMOD)
	public static AccessingPath.InitializationStrategy accessingPathInitStrategy;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean allowSelectingDeadEntities;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean allowTargetingSpecialEntities;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RESEARCH)
	public static boolean antiHostCheating;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean attackableTnt;
	
	@Option(defaultValue = "POSSIBLE", 
			parserClass = Confidence.Parser.class, 
			label = Label.MESSMOD)
	public static Confidence blameThreshold;
	
	@Option(defaultValue = "DISABLED", 
			parserClass = BlamingMode.Parser.class, 
			label = Label.MESSMOD)
	public static BlamingMode blamingMode;
	
	@Option(defaultValue = "NORMALLY", 
			parserClass = FrozenUpdateMode.Parser.class, 
			label = Label.MESSMOD)
	public static FrozenUpdateMode blockInfoRendererUpdateInFrozenTicks;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean blockPlacementHistory;
	
	@Option(defaultValue = "COLLISION", 
			parserClass = ShapeType.Parser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static ShapeType blockShapeToBeRendered;

	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RENDERER)
	public static boolean chunkLoadingInfoRenderer;
	
	@Option(defaultValue = "4", 
			parserClass = IntegerParser.NonNegative.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static int chunkLoadingInfoRenderRadius;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = Label.MESSMOD)
	public static boolean chunkLogAutoArchiving;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = Label.MESSMOD)
	public static boolean commandExecutionRequirment;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.REDSTONE)
	public static boolean craftingTableBUD;
	
	@Option(defaultValue = "NaN", 
			suggestions = {"0.05", "0.10", "NaN"}, 
			parserClass = FloatParser.NaNablePositive.class, 
			label = Label.INTERACTION_TWEAKS)
	public static float creativeUpwardsSpeed;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class, 
			label = { Label.INTERACTION_TWEAKS, Label.BUGFIX})
	public static boolean debugStickSkipsInvaildState;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean dedicatedServerCommands;
	
	@Option(defaultValue = "REGION,POI,ENTITY", 
			parserClass = SaveComponent.DefaultListParser.class, 
			suggestions = { "REGION,POI,ENTITY", "REGION,GAMERULES,ENTITY,POI", "[]" }, 
			label = Label.MESSMOD)
	public static List<SaveComponent> defaultSaveComponents;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean disableChunkLoadingCheckInCommands;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean disableEnchantCommandRestriction;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.EXPLOSION, Label.BREAKING_OPTIMIZATION })
	public static boolean disableExplosionExposureCalculation;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean disableItemUsageCooldown;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH})
	public static boolean disableProjectileRandomness;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH, Label.MESSMOD})
	public static boolean dumpTargetEntityDataOnClient;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH, Label.MESSMOD})
	public static boolean dumpTargetEntityDataWithPaper;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH, Label.MESSMOD})
	public static boolean dumpTargetEntityDataWithCtrlC;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH, Label.MESSMOD})
	public static boolean dumpTargetEntityNbt;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.RESEARCH, Label.MESSMOD})
	public static boolean dumpTargetEntitySummonCommand;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean enabledTools;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean endEyeTeleport;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class, 
			label = { Label.EXPLOSION, Label.RESEARCH })
	public static boolean entityExplosionInfluence;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.EXPLOSION, Label.RENDERER, Label.MESSMOD })
	public static boolean entityExplosionRaysVisiblity;
	
	@Option(defaultValue = "300", 
			parserClass = IntegerParser.class, 
			label = { Label.EXPLOSION, Label.RENDERER, Label.MESSMOD })
	public static int entityExplosionRaysLifetime;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = Label.MESSMOD)
	public static boolean entityLogAutoArchiving;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean expandedStructureBlockRenderingRange;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean fillHistory;
	
	@Option(defaultValue = "POSITIVE", 
			parserClass = PulseRecorder.Mode.Parser.class, 
			label = { Label.REDSTONE, Label.MESSMOD })
	public static PulseRecorder.Mode fletchingTablePulseDetectingMode;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.REDSTONE)
	public static boolean fletchingTablePulseDetector;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.CHUNK)
	public static boolean generateChunkGrid;
	
	@Option(defaultValue = "2.0", 
			experimental = true, 
			parserClass = FloatParser.Positive.class, 
			label = { Label.RESEARCH, Label.ENTITY }, 
			suggestions = { "2.0" })
	public static float getEntityRangeExpansion;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			globalOnly = true, 
			environment = EnvType.CLIENT, 
			label = { Label.MISC, Label.MESSMOD })
	public static boolean hideSurvivalSaves;
	
	@Option(defaultValue = "TOP_RIGHT", 
			parserClass = AlignMode.Parser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static AlignMode hudAlignMode;
	
	@Option(defaultValue = "(BL)^2/(mR)", 
			parserClass = StringParser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static String hudStyles;
	
	@Option(defaultValue = "1.0", 
			parserClass = FloatParser.Positive.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static float hudTextSize;
	
	@Option(defaultValue = "9", 
			parserClass = IntegerParser.HotbarLength.class, 
			label = Label.INTERACTION_TWEAKS)
	public static int hotbarLength;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean independentEntityPickerForInfomation;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean interactableB36;
	
	@Option(defaultValue = "-FOLLOW_SYSTEM_SETTINGS-", 
			parserClass = Language.Parser.class, 
			label = Label.MESSMOD)
	public static String language;
	
	@Option(defaultValue = "10", 
			parserClass = IntegerParser.Positive.class, 
			label = { Label.RESEARCH, Label.BUGFIX })
	public static int maxClientTicksPerFrame;
	
	@Option(defaultValue = "180", 
			parserClass = FloatParser.Positive.class, 
			label = { Label.INTERACTION_TWEAKS, Label.MESSMOD })
	public static float maxEndEyeTpRadius;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean minecartPlacementOnNonRailBlocks;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean mobFastKill;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.ENTITY, Label.BREAKING_OPTIMIZATION })
	public static boolean optimizedEntityPushing;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.CHUNK, Label.ENTITY })
	public static boolean projectileChunkLoading;
	
	@Option(defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class, 
			label = { Label.CHUNK, Label.ENTITY, Label.MESSMOD })
	public static int projectileChunkLoadingRange;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.CHUNK, Label.ENTITY, Label.MESSMOD })
	public static boolean projectileChunkLoadingPermanence;
	
	@Option(defaultValue = "1.0", 
			parserClass = FloatParser.class, 
			label = { Label.RESEARCH, Label.ENTITY })
	public static float projectileRandomnessScale;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean quickMobMounting;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean quickStackedEntityKilling;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.INTERACTION_TWEAKS)
	public static boolean railNoAutoConnection;
	
	@Option(defaultValue = "[]", 
			parserClass = ListParser.Ticket.class, 
			label = { Label.CHUNK, Label.RESEARCH })
	public static List<ChunkTicketType<?>> rejectChunkTicket;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RENDERER)
	public static boolean renderBlockShape;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RENDERER)
	public static boolean renderFluidShape;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RENDERER)
	public static boolean renderRedstoneGateInfo;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = Label.RENDERER)
	public static boolean serverSyncedBox;
	
	@Option(defaultValue = "-1", 
			parserClass = FloatParser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static float serverSyncedBoxRenderRange;
	
	@Option(defaultValue = "NORMALLY", 
			parserClass = FrozenUpdateMode.Parser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static FrozenUpdateMode serverSyncedBoxUpdateModeInFrozenTicks;
	
	@Option(defaultValue = "[]", 
			parserClass = RangeParser.ChunkStatusRange.class, 
			label = { Label.CHUNK, Label.RESEARCH })
	public static List<ChunkStatusSorter> skippedGenerationStages;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.CHUNK, Label.BREAKING_OPTIMIZATION })
	public static boolean skipUnloadedChunkInRaycasting;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class, 
			label = { Label.MESSMOD, Label.RENDERER })
	public static boolean stableHudLocation;
	
	// TODO
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class, 
			label = Label.MESSMOD)
	public static boolean strictAccessingPathParsing;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class, 
			label = Label.MISC)
	public static boolean superSuperSecretSetting;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.CHUNK, Label.ENTITY })
	public static boolean tntChunkLoading;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			label = { Label.CHUNK, Label.ENTITY, Label.MESSMOD })
	public static boolean tntChunkLoadingPermanence;
	
	@Option(defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class, 
			label = { Label.CHUNK, Label.ENTITY, Label.MESSMOD })
	public static int tntChunkLoadingRange;
	
	@Option(defaultValue = "[]", 
			parserClass = ListParser.DebugRender.class, 
			label = Label.RENDERER)
	public static List<Either<Field, String>> vanillaDebugRenderers;
	
	private static void setOptionSet(OptionSet set) {
		activeOptionSet = set;
		set.activiate();
		if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MessMod.LOGGER.info("Loaded {} MessMod config from {}", 
					OPTIONS.size(), set.getReadablePathStr());
			OPTIONS.values().stream()
					.map((o) -> {
						return o.name + ": " + set.getSerialized(o.name);
					})
					.forEach(MessMod.LOGGER::info);
		}
	}
	
	public static boolean isValidOptionName(String name) {
		return OPTIONS.containsKey(name);
	}
	
	public static void onReceivedOptions(PacketByteBuf in) throws IOException {
		setOptionSet(OptionSet.fromPacket(in));
	}
	
	private static void registerCustomApplicator(String name, CustomOptionApplicator behavior) {
		CUSTOM_APPLICATION_BEHAVIORS.put(name, behavior);
	}
	
	private static void registerCustomValidator(String name, CustomOptionValidator validator) {
		CUSTOM_OPTION_VALIDATORS.put(name, validator);
	}
	
	private static void registerCustomHandlers(String name, CustomOptionValidator validator, 
			CustomOptionApplicator behavior) {
		CUSTOM_APPLICATION_BEHAVIORS.put(name, behavior);
		CUSTOM_OPTION_VALIDATORS.put(name, validator);
	}
	
	/**
	 * @param ms The current single-player server, or null if exiting.
	 */
	public static void updateServer(@Nullable MinecraftServer ms) {
		if(ms != null) {
			Path p = ms.getSavePath(WorldSavePathMixin.create("mcwmem.prop"));
			setOptionSet(OptionSet.load(p.toFile()));
		} else {
			activeOptionSet.save();
			setOptionSet(OptionSet.GLOBAL);
		}
	}
	
	public static void loadFromRemoteServer(PacketByteBuf data) {
		setOptionSet(OptionSet.fromPacket(data));
	}
	
	public static void loadSingleFromRemoteServer(PacketByteBuf data) {
		if(activeOptionSet == OptionSet.GLOBAL) {
			MessMod.LOGGER.error("Trying to load options to global option set!");
			return;
		}
		
		String name = data.readString();
		String value = data.readString();
		try {
			activeOptionSet.set(name, value);
		} catch (InvalidOptionException e) {
			MessMod.LOGGER.error("Received incorrect option {}={}: {}", name, value, e.getLocalizedMessage());
		}
	}
	
	public static void reload() {
		activeOptionSet.reload();
	}
	
	public static void sendOptionsTo(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(activeOptionSet.toPacket());
	}
	
	public static OptionSet getActiveOptionSet() {
		return activeOptionSet;
	}
	
	public static OptionSet getGlobalOptionSet() {
		return OptionSet.GLOBAL;
	}
	
	static{
		registerCustomHandlers("enabledTools", (newValue, ct) -> {
			if(!FabricLoader.getInstance().isModLoaded("carpet")) {
				throw new InvalidOptionException("opt.err.reqcarpet");
			}
		}, (newValue, ct) -> {
			if(ct == null) {
				// We cannot apply the change without a context.
				return;
			}
			
			// XXX Only influences the sender?
			if((Boolean) newValue) {
				CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script load tool global");
			}else {
				CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script unload tool");
			}
		});
		CustomOptionValidator checkLithium = (newVal, ct) -> {
			if (FabricLoader.getInstance().isModLoaded("lithium") && (Boolean) newVal) {
				throw new InvalidOptionException("opt.err.lithium");
			}
		};
		registerCustomValidator("entityExplosionInfluence", checkLithium);
		registerCustomValidator("disableExplosionExposureCalculation", checkLithium);
		CustomOptionValidator requireCarpet = (newVal, ct) -> {
			if (!FabricLoader.getInstance().isModLoaded("carpet") 
					&& (!(newVal instanceof Boolean) || (Boolean) newVal)) {
				throw new InvalidOptionException("opt.err.reqcarpet");
			}
		};
		registerCustomValidator("blockInfoRendererUpdateInFrozenTicks", requireCarpet);
		registerCustomApplicator("hudStyles", (newVal, ct) -> {
			if (!MessMod.isDedicatedServerEnv() && MessMod.INSTANCE.getClientHudManager() != null) {
				MessMod.INSTANCE.getClientHudManager().updateStyle((String) newVal);
			}
		});
		registerCustomHandlers("language", (newVal, ct) -> {
			boolean forceLoad = ((String) newVal).endsWith(Language.FORCELOAD_SUFFIX);
			String id = ((String) newVal).replace(Language.FORCELOAD_SUFFIX, "");
			if(!I18N.canUseLanguage(id, forceLoad)) {
				throw new InvalidOptionException("Language " + id + " is unsupported or incomplete.");
			}
		}, (newVal, ct) -> {
			boolean forceLoad = ((String) newVal).endsWith(Language.FORCELOAD_SUFFIX);
			String id = ((String) newVal).replace(Language.FORCELOAD_SUFFIX, "");
			if(!I18N.setLanguage(id, forceLoad)) {
				throw new IllegalStateException("Option language is not validated!");
			}
		});
		// TODO Use less hard-coded validator
		registerCustomValidator("expandedStructureBlockRenderingRange", (newVal, ct) -> {
			MutableBoolean conflict = new MutableBoolean(false);
			FabricLoader.getInstance().getModContainer("carpet").ifPresent((mod) -> {
				Version ver = mod.getMetadata().getVersion();
				if(ver instanceof SemanticVersion) {
					try {
						conflict.setValue(SemanticVersion.parse("1.4.24").compareTo(ver) <= 0);
					} catch (VersionParsingException e) {
					}
				}
			});
			if(conflict.booleanValue()) {
				throw new InvalidOptionException("opt.err.conflict.carpet.1425");
			}
		});
		OPTIONS.values().forEach((o) -> {
			if(!I18N.EN_US.containsKey(String.format("opt.%s.desc", o.name))) {
				MessMod.LOGGER.warn("The description of option {} is missing!", o.name);
			}
		});
		setOptionSet(OptionSet.GLOBAL);
	}
	
	@FunctionalInterface interface CustomOptionApplicator {
		/**
		 * Called after the new value of an option has been set. 
		 * This method mainly perform custom application steps other than changing the value of an option.
		 * @param newValue The new value, previously validated.
		 */
		void onOptionUpdate(Object newValue, @Nullable CommandContext<ServerCommandSource> ct);
	}
	
	@FunctionalInterface interface CustomOptionValidator {
		/**
		 * Perform some non-generic validations.
		 * @param oldValue The previous value of the updated option, or null not exist.
		 */
		void validate(@Nullable Object newVal, @Nullable CommandContext<ServerCommandSource> ct)
				throws InvalidOptionException;
	}
}
