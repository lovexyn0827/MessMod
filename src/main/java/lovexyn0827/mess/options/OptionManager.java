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

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.options.RangeParser.ChunkStatusRange.ChunkStatusSorter;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.BlockInfoRenderer.ShapeType;
import lovexyn0827.mess.rendering.hud.AlignMode;
import lovexyn0827.mess.util.PulseRecorder;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.blame.BlamingMode;
import lovexyn0827.mess.util.blame.Confidence;
import lovexyn0827.mess.util.i18n.I18N;
import lovexyn0827.mess.util.i18n.Language;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
// TODO Now the option manager is pretty messy!
public class OptionManager{
	public static final SortedMap<String, OptionWrapper> OPTIONS = Stream.of(OptionManager.class.getFields())
			.filter((f) -> f.isAnnotationPresent(Option.class))
			.sorted((a, b) -> Comparator.<String>naturalOrder().compare(a.getName(), b.getName()))
			.collect(TreeMap::new, (map, f) -> map.put(f.getName(), new OptionWrapper(f)), Map::putAll);
	private static OptionSet activeOptionSet;
	
	/**
	 * Actions taken right after an option is set to a given value.
	 */
	static final Map<String, CustomAction> CUSTOM_APPLICATION_BEHAVIORS = Maps.newHashMap();
	
	@Option(defaultValue = "STANDARD", 
			parserClass = AccessingPath.InitializationStrategy.Parser.class)
	public static AccessingPath.InitializationStrategy accessingPathInitStrategy;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean allowSelectingDeadEntities;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean allowTargetingSpecialEntities;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean antiHostCheating;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean attackableTnt;
	
	@Option(defaultValue = "POSSIBLE", 
			parserClass = Confidence.Parser.class)
	public static Confidence blameThreshold;
	
	@Option(defaultValue = "DISABLED", 
			parserClass = BlamingMode.Parser.class)
	public static BlamingMode blamingMode;
	
	@Option(defaultValue = "NORMALLY", 
			parserClass = BlockInfoRenderer.FrozenUpdateMode.Parser.class)
	public static BlockInfoRenderer.FrozenUpdateMode blockInfoRendererUpdateInFrozenTicks;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean blockPlacementHistory;
	
	@Option(defaultValue = "COLLISION", 
			parserClass = ShapeType.Parser.class)
	public static ShapeType blockShapeToBeRendered;

	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean chunkLoadingInfoRenderer;
	
	@Option(defaultValue = "4", 
			parserClass = IntegerParser.NonNegative.class)
	public static int chunkLoadingInfoRenderRadius;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean chunkLogAutoArchiving;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean commandExecutionRequirment;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean craftingTableBUD;
	
	@Option(defaultValue = "NaN", 
			suggestions = {"0.05", "0.10", "NaN"}, 
			parserClass = FloatParser.NaNablePositive.class)
	public static float creativeUpwardsSpeed;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean debugStickSkipsInvaildState;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean dedicatedServerCommands;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableChunkLoadingCheckInCommands;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableEnchantCommandRestriction;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableExplosionExposureCalculation;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean disableProjectileRandomness;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean enabledTools;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean endEyeTeleport;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean entityExplosionInfluence;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean entityExplosionRaysVisiblity;
	
	@Option(defaultValue = "300", 
			parserClass = IntegerParser.class)
	public static int entityExplosionRaysLifetime;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean entityLogAutoArchiving;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean fillHistory;
	
	@Option(defaultValue = "POSITIVE", 
			parserClass = PulseRecorder.Mode.Parser.class)
	public static PulseRecorder.Mode fletchingTablePulseDetectingMode;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean fletchingTablePulseDetector;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean generateChunkGrid;
	
	// TODO Assess the performance cost
	@Option(defaultValue = "2.0", 
			experimental = true, 
			parserClass = FloatParser.Positive.class)
	public static float getEntityRangeExpansion;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class, 
			globalOnly = true, 
			environment = EnvType.CLIENT)
	public static boolean hideSurvivalSaves;
	
	@Option(defaultValue = "TOP_RIGHT", 
			parserClass = AlignMode.Parser.class)
	public static AlignMode hudAlignMode;
	
	@Option(defaultValue = "(BL)^2/(mR)", 
			parserClass = StringParser.class)
	public static String hudStyles;
	
	@Option(defaultValue = "1.0", 
			parserClass = FloatParser.Positive.class, 
			experimental = true)
	public static float hudTextSize;
	
	@Option(defaultValue = "9", 
			parserClass = IntegerParser.HotbarLength.class)
	public static int hotbarLength;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean interactableB36;
	
	@Option(defaultValue = "-FOLLOW_SYSTEM_SETTINGS-", 
			parserClass = Language.Parser.class)
	public static String language;
	
	@Option(defaultValue = "10", 
			parserClass = IntegerParser.Positive.class)
	public static int maxClientTicksPerFrame;
	
	@Option(defaultValue = "180", 
			parserClass = FloatParser.Positive.class)
	public static float maxEndEyeTpRadius;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean minecartPlacementOnNonRailBlocks;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean mobFastKill;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean optimizedEntityPushing;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean projectileChunkLoading;
	
	@Option(defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class)
	public static int projectileChunkLoadingRange;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean projectileChunkLoadingPermanence;
	
	@Option(defaultValue = "1.0", 
			parserClass = FloatParser.class)
	public static float projectileRandomnessScale;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean quickMobMounting;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean railNoAutoConnection;
	
	@Option(defaultValue = "[]", 
			parserClass = ListParser.Ticket.class)
	public static List<ChunkTicketType<?>> rejectChunkTicket;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderBlockShape;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderFluidShape;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean renderRedstoneGateInfo;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean serverSyncedBox;
	
	@Option(defaultValue = "-1", 
			parserClass = FloatParser.class)
	public static float serverSyncedBoxRenderRange;
	
	@Option(defaultValue = "[]", 
			parserClass = RangeParser.ChunkStatusRange.class)
	public static List<ChunkStatusSorter> skippedGenerationStages;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean skipUnloadedChunkInRaycasting;
	
	@Option(defaultValue = "true", 
			parserClass = BooleanParser.class)
	public static boolean stableHudLocation;
	
	// TODO
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean strictAccessingPathParsing;
	
	@Option(defaultValue = "false", 
			experimental = true, 
			parserClass = BooleanParser.class)
	public static boolean superSuperSecretSetting;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean tntChunkLoading;
	
	@Option(defaultValue = "false", 
			parserClass = BooleanParser.class)
	public static boolean tntChunkLoadingPermanence;
	
	@Option(defaultValue = "3", 
			parserClass = IntegerParser.NonNegative.class)
	public static int tntChunkLoadingRange;
	
	@Option(defaultValue = "[]", 
			parserClass = ListParser.DebugRender.class)
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
	
	private static void registerCustomApplicationBehavior(String name, @Nullable CustomAction behavior) {
		CUSTOM_APPLICATION_BEHAVIORS.put(name, behavior);
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
	
	public static String getSerialized(String name) {
		return activeOptionSet.getSerialized(name);
	}
	
	public static String getGlobalSerialized(String name) {
		return OptionSet.GLOBAL.getSerialized(name);
	}
	
	public static void reload() {
		activeOptionSet.reload();
	}
	
	public static void set(String name, String val, 
			CommandContext<ServerCommandSource> ct) throws InvalidOptionException {
		activeOptionSet.set(name, val);
	}
	
	public static void setGlobal(String name, String val, 
			CommandContext<ServerCommandSource> ct) throws InvalidOptionException {
		OptionSet.GLOBAL.set(name, val);
	}
	
	public static void sendOptionsTo(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(activeOptionSet.toPacket());
	}
	
	static{
		registerCustomApplicationBehavior("enabledTools", (oldValue, newValue, ct) -> {
			if(ct == null) {
				return;
			}
			
			if(!FabricLoader.getInstance().isModLoaded("carpet")) {
				throw new InvalidOptionException("opt.err.reqcarpet");
			}
			
			// FIXME Only influences the sender?
			if((Boolean) newValue) {
				CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script load tool");
			}else {
				CommandUtil.execute(CommandUtil.noreplySourceFor(ct.getSource()), "/script unload tool");
			}
		});
		CustomAction checkLithium = (oldVal, newVal, ct) -> {
			if (FabricLoader.getInstance().isModLoaded("lithium") && (Boolean) newVal) {
				throw new InvalidOptionException("opt.err.lithium");
			}
		};
		registerCustomApplicationBehavior("entityExplosionInfluence", checkLithium);
		registerCustomApplicationBehavior("disableExplosionExposureCalculation", checkLithium);
		CustomAction requireCarpet = (oldVal, newVal, ct) -> {
			if (!FabricLoader.getInstance().isModLoaded("carpet") 
					&& (!(newVal instanceof Boolean) || (Boolean) newVal)) {
				throw new InvalidOptionException("opt.err.reqcarpet");
			}
		};
		registerCustomApplicationBehavior("blockInfoRendererUpdateInFrozenTicks", requireCarpet);
		registerCustomApplicationBehavior("hudStyles", (oldVal, newVal, ct) -> {
			if (!MessMod.isDedicatedServerEnv() && MessMod.INSTANCE.getClientHudManager() != null) {
				MessMod.INSTANCE.getClientHudManager().updateStyle((String) newVal);
			}
		});
		registerCustomApplicationBehavior("language", (oldVal, newVal, ct) -> {
			boolean forceLoad = ((String) newVal).endsWith(Language.FORCELOAD_SUFFIX);
			String id = ((String) newVal).replace(Language.FORCELOAD_SUFFIX, "");
			if(!I18N.setLanguage(id, forceLoad)) {
				// Needn't be translated
				throw new InvalidOptionException("Language " + id + " is unsupported or incomplete.");
			}
		});
		OPTIONS.values().forEach((o) -> {
			if(!I18N.EN_US.containsKey(String.format("opt.%s.desc", o.name))) {
				MessMod.LOGGER.warn("The description of option {} is missing!", o.name);
			}
		});
		setOptionSet(OptionSet.GLOBAL);
	}
	
	@FunctionalInterface interface CustomAction {
		/**
		 * Called after the new value of an option has been set. This method mainly perform some validations, and
		 * some custom application steps.
		 * @param oldValue The previous value of the updated option, or null not exist.
		 */
		void onOptionUpdate(@Nullable Object oldValue, Object newValue, 
				@Nullable CommandContext<ServerCommandSource> ct)throws InvalidOptionException;
	}
}
