package lovexyn0827.mess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.fakes.DebugRendererEnableState;
import lovexyn0827.mess.log.EntityLogger;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.network.MessClientNetworkHandler;
import lovexyn0827.mess.network.MessServerNetworkHandler;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.ServerSyncedBoxRenderer;
import lovexyn0827.mess.rendering.ShapeCache;
import lovexyn0827.mess.rendering.ShapeRenderer;
import lovexyn0827.mess.rendering.ShapeSender;
import lovexyn0827.mess.rendering.hud.ClientHudManager;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import lovexyn0827.mess.rendering.hud.ServerHudManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import lovexyn0827.mess.util.deobfuscating.MappingProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.LiteralText;

public class MessMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final MessMod INSTANCE = new MessMod();
	private Mapping mapping;
	@Environment(EnvType.CLIENT)
	private ClientHudManager hudManagerC;
	private ServerHudManager hudManagerS;
	private ServerSyncedBoxRenderer boxRenderer;
	private MinecraftServer server;
	private String scriptDir;
	@Environment(EnvType.CLIENT)
	public ShapeRenderer shapeRenderer;	// Reading from the field directly may bring higher performance.
	@Environment(EnvType.CLIENT)
	public ShapeCache shapeCache;
	public ShapeSender shapeSender;
	private BlockInfoRenderer blockInfoRederer = new BlockInfoRenderer();
	private EntityLogger logger;
	@Environment(EnvType.CLIENT)
	private MessClientNetworkHandler clientNetworkHandler;
	private MessServerNetworkHandler serverNetworkHandler;
	private BlockPlacementHistory placementHistory;

	private MessMod() {
		this.boxRenderer = new ServerSyncedBoxRenderer();
		this.logger = new EntityLogger();
		this.reloadMapping();
		ServerPlayerInteractionManager.class.getAnnotatedInterfaces();
	}

	public void reloadMapping() {
		this.mapping = new MappingProvider().tryLoadMapping();
	}

	@Override
	public void onInitialize() {
	}
	
	public Mapping getMapping() {
		return this.mapping;
	}
	
	private void copyScript(String name) throws IOException {
		Path scriptPath = Paths.get(this.scriptDir);
		if(!Files.exists(scriptPath)) {
			Files.createDirectories(scriptPath);
		}
		
		// TODO
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
		try {
			this.logger.tick(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void onServerStarted(MinecraftServer server) {
		this.server = server;
		CommandUtil.updateServer(server);
		OptionManager.updateServer(server);
		this.serverNetworkHandler = new MessServerNetworkHandler(server);
		this.shapeSender = ShapeSender.create(server);
		this.boxRenderer.setServer(server);
		this.blockInfoRederer.initializate(server);
		this.hudManagerS = new ServerHudManager(server);
		this.placementHistory = new BlockPlacementHistory();
		try {
			this.logger.initialize(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onServerShutdown(MinecraftServer server) {
		this.boxRenderer.uninitialize();
		this.server = null;
		this.logger.closeAll();
		this.hudManagerS = null;
		this.logger.closeAll();
		this.serverNetworkHandler = null;
		this.placementHistory = new BlockPlacementHistory();
		if(OptionManager.entityLogAutoArchiving) {
			try {
				this.logger.archiveLogs();
			} catch (IOException e) {
				LOGGER.error("Failed to archive entity logs!");
				e.printStackTrace();
			}
		}
		
		CommandUtil.updateServer(null);
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
		((DebugRendererEnableState) (mc.debugRenderer)).update();
        this.shapeRenderer = sr;
        this.shapeCache = sr.getShapeCache();
		this.hudManagerC = new ClientHudManager();
		this.hudManagerC.playerHudS = new PlayerHud(this.hudManagerC, player, true);
		if (!isDedicatedEnv()) {
			this.hudManagerS.playerHudC.updatePlayer();
		}
	}

	@Environment(EnvType.CLIENT)
	public void onClientTicked() {
		ServerHudManager shm = this.getServerHudManager();
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
		String merged = "";
		for(Object ob : message) {
			merged += ob;
		}
		
		this.server.getPlayerManager().broadcastChatMessage(new LiteralText(merged), 
				MessageType.SYSTEM, 
				new UUID(0x31f38bL,0x31f0b8L));
	}

	public String getScriptDir() {
		return this.scriptDir;
	}
	
	public EntityLogger getEntityLogger() {
		return this.logger;
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

	public BlockPlacementHistory getPlacementHistory() {
		return this.placementHistory;
	}
}
