package lovexyn0827.mess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.entity.EntityLogger;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.network.MessClientNetworkHandler;
import lovexyn0827.mess.network.MessServerNetworkHandler;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.ChunkLoadingInfoRenderer;
import lovexyn0827.mess.rendering.ServerSyncedBoxRenderer;
import lovexyn0827.mess.rendering.ShapeCache;
import lovexyn0827.mess.rendering.ShapeRenderer;
import lovexyn0827.mess.rendering.ShapeSender;
import lovexyn0827.mess.rendering.hud.ClientHudManager;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import lovexyn0827.mess.rendering.hud.ServerHudManager;
import lovexyn0827.mess.util.access.CustomNode;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import lovexyn0827.mess.util.deobfuscating.MappingProvider;
import lovexyn0827.mess.util.phase.ClientTickingPhase;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class MessMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final MessMod INSTANCE = new MessMod();
	private Mapping mapping;
	@Environment(EnvType.CLIENT)
	private ClientHudManager hudManagerC;
	private ServerHudManager hudManagerS;
	private ServerSyncedBoxRenderer boxRenderer;
	@Nullable
	private MinecraftServer server;
	private String scriptDir;
	@Environment(EnvType.CLIENT)
	public ShapeRenderer shapeRenderer;	// Reading from the field directly may bring higher performance.
	@Environment(EnvType.CLIENT)
	public ShapeCache shapeCache;
	public ShapeSender shapeSender;
	private BlockInfoRenderer blockInfoRederer = new BlockInfoRenderer();
	private EntityLogger entityLogger;
	@Environment(EnvType.CLIENT)
	private MessClientNetworkHandler clientNetworkHandler;
	private MessServerNetworkHandler serverNetworkHandler;
	private ChunkLoadingInfoRenderer chunkLoadingInfoRenderer;
	private ChunkBehaviorLogger chunkLogger;
	private long gameTime;

	private MessMod() {
		this.reloadMapping();
	}

	public void reloadMapping() {
		this.mapping = new MappingProvider().tryLoadMapping();
	}

	@Override
	public void onInitialize() {
		OptionManager.loadGlobal();
	}
	
	public Mapping getMapping() {
		return this.mapping;
	}
	
	private void copyScript(String name) throws IOException {
		Path scriptPath = Paths.get(this.scriptDir);
		if(!Files.exists(scriptPath)) {
			Files.createDirectories(scriptPath);
		}
		
		Files.copy(MessMod.class.getResourceAsStream("/assets/scarpet/" + name + ".sc"), 
				Paths.get(this.scriptDir, name + ".sc"), 
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	//************ SERVER SIDE *****************
	
	public void onServerTicked(MinecraftServer server) {
		if(this.hudManagerS != null) {
			this.hudManagerS.tick(server);
		}
		
		this.boxRenderer.tick();
		this.blockInfoRederer.tick();
		this.shapeSender.updateClientTime(server.getOverworld().getTime());
		this.entityLogger.serverTick();
	}
	

	public void onServerStarted(MinecraftServer server) {
		this.server = server;
		CommandUtil.updateServer(server);
		OptionManager.updateServer(server);
		this.serverNetworkHandler = new MessServerNetworkHandler(server);
		this.boxRenderer = new ServerSyncedBoxRenderer(server);
		this.blockInfoRederer.initializate(server);
		this.hudManagerS = new ServerHudManager(server);
		CustomNode.reload(server);
		this.chunkLoadingInfoRenderer = new ChunkLoadingInfoRenderer();
		this.entityLogger = new EntityLogger(server);
		this.shapeSender = ShapeSender.create(server);
		this.chunkLogger = new ChunkBehaviorLogger(server);
	}

	public void onServerShutdown(MinecraftServer server) {
		this.boxRenderer = null;
		this.server = null;
		this.entityLogger.closeAll();
		this.hudManagerS = null;
		this.entityLogger.closeAll();
		this.serverNetworkHandler = null;
		this.chunkLoadingInfoRenderer.close();
		this.chunkLoadingInfoRenderer = null;
		ServerTickingPhase.initialize();
		if(OptionManager.entityLogAutoArchiving) {
			try {
				this.entityLogger.archiveLogs();
			} catch (IOException e) {
				LOGGER.error("Failed to archive entity logs!");
				e.printStackTrace();
			}
		}
		
		if(OptionManager.chunkLogAutoArchiving) {
			try {
				this.chunkLogger.archiveLogs();
			} catch (IOException e) {
				LOGGER.error("Failed to archive entity logs!");
				e.printStackTrace();
			}
		}
		
		this.entityLogger = null;
		this.chunkLogger = null;
		CommandUtil.updateServer(null);
		OptionManager.loadGlobal();
	}

	public void onServerPlayerSpawned(ServerPlayerEntity player) {
		CommandUtil.tryUpdatePlayer(player);
		try {
			this.scriptDir = server.getSavePath(WorldSavePathMixin.create("scripts")).toAbsolutePath().toString();
			copyScript("tool");
			if(FabricLoader.getInstance().isModLoaded("carpet")) {
				if(OptionManager.enabledTools) {
					this.server.getCommandManager().execute(CommandUtil.noreplyPlayerSources(), 
							"/script load tool global");
				}
			}
			OptionManager.sendOptionsTo(player);
		} catch (IOException e) {
			LOGGER.error("Scarpet scripts couldn't be loaded.");
			e.printStackTrace();
		}
	}

	//************ CLIENT SIDE *****************
	
	@Environment(EnvType.CLIENT)
	public void onRender(ClientPlayerEntity player, IntegratedServer server) {
		if(this.getClientHudManager() != null) this.getClientHudManager().render(player,server);
	}
	
	@Environment(EnvType.CLIENT)
	public void onGameJoined(GameJoinS2CPacket packet) {
		MinecraftClient mc = MinecraftClient.getInstance();
		this.clientNetworkHandler = new MessClientNetworkHandler(mc);
		this.clientNetworkHandler.sendVersion();
		ClientPlayerEntity player = mc.player;
		ShapeRenderer sr = new ShapeRenderer(mc);
        this.shapeRenderer = sr;
        this.shapeCache = sr.getShapeCache();
		this.hudManagerC = new ClientHudManager();
		this.hudManagerC.playerHudS = new PlayerHud(this.hudManagerC, player, true);
		if (!isDedicatedEnv()) {
			this.hudManagerS.playerHudC.updatePlayer();
		}
	}

	@Environment(EnvType.CLIENT)
	public void onClientTickStart() {
		ClientTickingPhase.CLIENT_TICK_START.begin(null);
	}

	@Environment(EnvType.CLIENT)
	public void onClientTicked() {
		ClientTickingPhase.CLIENT_TICK_END.begin(null);
		ServerHudManager shm = this.getServerHudManager();
		if (this.entityLogger != null) {
			this.entityLogger.clientTick();
		}
		
		if(shm != null && shm.playerHudC != null) {
			shm.playerHudC.updateData();
		}
	}

	//Client
	@Environment(EnvType.CLIENT)
	public void onDisconnected() {
		this.hudManagerC = null;
	}
	
	//Client
	@Environment(EnvType.CLIENT)
	public void onPlayerRespawned(PlayerRespawnS2CPacket packet) {
		this.getServerHudManager().playerHudC.updatePlayer();
	}

	public void sendMessageToEveryone(Object... message) {
		if(this.server == null) {
			throw new IllegalStateException("Called without a server started!");
		}
		
		StringBuilder sb = new StringBuilder();
		for(Object ob : message) {
			sb.append(ob);
		}
		
		this.server.getPlayerManager().broadcastChatMessage(new LiteralText(sb.toString()), 
				MessageType.SYSTEM, 
				new UUID(0x31f38bL,0x31f0b8L));
	}

	public String getScriptDir() {
		return this.scriptDir;
	}
	
	public EntityLogger getEntityLogger() {
		return this.entityLogger;
	}

	@Environment(EnvType.CLIENT)
	public ClientHudManager getClientHudManager() {
		return hudManagerC;
	}
	
	public ServerHudManager getServerHudManager() {
		return hudManagerS;
	}
	
	/**
	 * @return Whether or not the running environment is a dedicated server or a client that has joined to a dedicated server
	 */
	public static boolean isDedicatedEnv() {
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			return true;
		} else {
			MinecraftClient mc = MinecraftClient.getInstance();
			return mc.getServer() == null && (mc.getCurrentServerEntry() == null ? 
					false : !mc.getCurrentServerEntry().isLocal());
		}
	}

	@Environment(EnvType.CLIENT)
	public void onDisconnect(DisconnectS2CPacket packet) {
		this.shapeCache.reset();
		this.clientNetworkHandler = null;
	}

	public MessServerNetworkHandler getServerNetworkHandler() {
		return this.serverNetworkHandler;
	}

	@Environment(EnvType.CLIENT)
	public MessClientNetworkHandler getClientNetworkHandler() {
		return this.clientNetworkHandler;
	}
	
	public static boolean isDedicatedServerEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
	}

	public boolean isOnThread(NetworkSide side) {
		if(side == NetworkSide.CLIENTBOUND) {
			return MinecraftClient.getInstance().isOnThread();
		} else {
			return this.server != null ? this.server.isOnThread() : false;
		}
	}

	public ChunkBehaviorLogger getChunkLogger() {
		return this.chunkLogger;
	}

	public long getGameTime() {
		return this.gameTime;
	}

	public void updateTime(long time) {
		this.gameTime = time;
	}
}
