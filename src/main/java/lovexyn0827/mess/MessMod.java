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
import lovexyn0827.mess.log.EntityLogger;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.ServerSyncedBoxRenderer;
import lovexyn0827.mess.rendering.ShapeRenderer;
import lovexyn0827.mess.rendering.hud.ClientHudManager;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import lovexyn0827.mess.rendering.hud.ServerHudManager;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import lovexyn0827.mess.util.deobfuscating.MappingProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.network.MessageType;
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
	private ClientHudManager hudManagerC;
	private ServerHudManager hudManagerS;
	private int windowX;
	private int windowY;
	private ServerSyncedBoxRenderer boxRenderer;
	private MinecraftServer server;
	private String scriptDir;
	private long gameTime;
	public ShapeRenderer shapeRenderer;	// Reading from the field directly may bring higher performance.
	private BlockInfoRenderer blockInfoRederer = new BlockInfoRenderer();
	private EntityLogger logger;

	private MessMod() {
		this.boxRenderer = new ServerSyncedBoxRenderer();
		this.logger = new EntityLogger();
		this.reloadMapping();
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
	
	public long getGameTime() {
		if(this.server != null) {
			return this.gameTime;
		} else {
			return -1;
		}
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
	
	public void onRender(ClientPlayerEntity player, IntegratedServer server) {
		this.updateWindowSize(MinecraftClient.getInstance().getWindow());
		if(this.getClientHudManager() != null) this.getClientHudManager().render(player,server);
	}

	private void updateWindowSize(Window window) {
		this.windowX = window.getScaledWidth();
		this.windowY = window.getScaledHeight();
	}
	
	public int getWindowX() {
		return this.windowX;
	}
	
	public int getWindowY() {
		return this.windowY;
	}
	
	public void onGameJoined(GameJoinS2CPacket packet) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		this.hudManagerC = new ClientHudManager();
		this.hudManagerC.playerHudS = new PlayerHud(this.hudManagerC, player, true);
		this.hudManagerS.playerHudC.updatePlayer();
	}
	
	public void onServerTicked(MinecraftServer server) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		ServerHudManager shm = this.getServerHudManager();
		if(player != null && shm != null && shm.lookingHud != null) {
			shm.lookingHud.updateLookingAtEntityData(player);
		}
		
//		if(shm != null && shm.playerHudS == null && player != null) {
//			this.getServerHudManager().playerHudS = new PlayerHud(this.getServerHudManager(), player, true);
//			this.getServerHudManager().playerHudS = (PlayerHudDataSenderer) HudDataSenderer
//					.createHudDataSenderer(this.server.isDedicated(), HudType.SERVER_PLAYER);
//		}
		
		if(player != null && shm != null && shm.playerHudS != null) {
			shm.playerHudS.updateData(player);
		}
		
		this.boxRenderer.tick();
		this.gameTime = this.server.getOverworld().getTime();
		//System.out.println(this.gameTime);
		this.blockInfoRederer.tick();
		try {
			this.logger.tick(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void onClientTicked() {
		ServerHudManager shm = this.getServerHudManager();
		if(shm != null && shm.playerHudC != null) {
			shm.playerHudC.updateData();
		}else {
			this.hudManagerS = new ServerHudManager();
		}
	}

	//Client
	public void onDisconnected() {
		this.hudManagerC = null;
	}
	
	//Client
	public void onPlayerRespawned(PlayerRespawnS2CPacket packet) {
		this.getServerHudManager().playerHudC.updatePlayer();
	}

	public void onServerStarted(MinecraftServer server) {
		this.server = server;
		CommandUtil.updateServer(server);
		OptionManager.updateServer(server);
		this.boxRenderer.setServer(server);
		this.blockInfoRederer.initializate(server);
		this.hudManagerS = new ServerHudManager();
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
		if(OptionManager.entityLogAutoArchiving) {
			try {
				this.logger.archiveLogs();
			} catch (IOException e) {
				LOGGER.error("Failed to archive entity logs!");
				e.printStackTrace();
			}
		}
		
		CommandUtil.updateServer(null);
		this.shapeRenderer.reset();
	}

	public void onServerPlayerSpawned(ServerPlayerEntity player) {
		//this.hudManager.playerHudS = new PlayerHud(this.hudManager, player, true);
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

	public ShapeRenderer getShapeRenderer() {
		return this.shapeRenderer;
	}
	
	public EntityLogger getEntityLogger() {
		return this.logger;
	}

	public ClientHudManager getClientHudManager() {
		return hudManagerC;
	}
	
	public ServerHudManager getServerHudManager() {
		return hudManagerS;
	}
	
	public boolean isDedicatedEnv() {
		return this.server != null && this.server.isDedicated();
	}
}
