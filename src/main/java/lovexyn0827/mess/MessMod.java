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
import lovexyn0827.mess.rendering.hud.HudManager;
import lovexyn0827.mess.rendering.hud.PlayerHud;
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
	private HudManager hudManager;
	private int windowX;
	private int windowY;
	private ServerSyncedBoxRenderer boxRenderer;
	private MinecraftServer server;
	private String scriptDir;
	private long gameTime;
	public ShapeRenderer shapeRenderer;	// Reading from the field directly may bring high performance.
	private BlockInfoRenderer blockInfoRederer = new BlockInfoRenderer();
	private EntityLogger logger;

	private MessMod() {
		this.boxRenderer = new ServerSyncedBoxRenderer();
		this.logger = new EntityLogger();
		this.reloadMapping();
		OptionManager.OPTIONS.toString();
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
		
		Files.copy(MessMod.class.getResourceAsStream("/assets/scarpet/" + name + ".sc"), 
				Paths.get(this.scriptDir, name + ".sc"), 
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void onRender(ClientPlayerEntity player, IntegratedServer server) {
		this.updateWindowSize(MinecraftClient.getInstance().getWindow());
		if(this.getHudManager() != null) this.getHudManager().render(player,server);
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
		this.hudManager = new HudManager();
	}
	
	public void onServerTicked(MinecraftServer server) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		if(player != null && MessMod.INSTANCE.getHudManager() != null) {
			MessMod.INSTANCE.getHudManager().lookingHud.updateData(player);
		}if(this.getHudManager() != null && this.getHudManager().playerHudS == null && player != null) {
			this.getHudManager().playerHudS = new PlayerHud(this.getHudManager(), player, true);
		}
		
		if(player != null && this.getHudManager() != null && this.getHudManager().playerHudS != null) {
			this.getHudManager().playerHudS.updateData(player);
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
		if(this.getHudManager() != null) {
			this.getHudManager().playerHudC.updateData();
		}else {
			this.hudManager = new HudManager();
		}
	}

	public void onDisconnected() {
		this.hudManager = null;
	}
	
	public void onPlayerRespawned(PlayerRespawnS2CPacket packet) {
		this.getHudManager().playerHudC.refreshPlayer();
	}

	public void onServerStarted(MinecraftServer server) {
		CommandUtil.updateServer(server);
		this.server = server;
		OptionManager.updateServer(server);
		this.boxRenderer.setServer(server);
		this.blockInfoRederer.initializate(server);
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
		System.out.println(player.getEntityName() + " spawned");
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

	public HudManager getHudManager() {
		return hudManager;
	}
}
