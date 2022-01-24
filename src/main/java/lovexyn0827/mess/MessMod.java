package lovexyn0827.mess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lovexyn0827.mess.command.CommandUtil;
import lovexyn0827.mess.deobfuscating.DummyMapping;
import lovexyn0827.mess.deobfuscating.Mapping;
import lovexyn0827.mess.deobfuscating.TinyMapping;
import lovexyn0827.mess.log.EntityLogger;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.ServerSyncedBoxRenderer;
import lovexyn0827.mess.rendering.ShapeRenderer;
import lovexyn0827.mess.rendering.hud.HudManager;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
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
	public Mapping mapping;
	public HudManager hudManager;
	public Options options;
	private int windowX;
	private int windowY;
	private ServerSyncedBoxRenderer boxRenderer;
	private MinecraftServer server;
	private String scriptDir;
	private long gameTime;
	public ShapeRenderer shapeRenderer;
	private EntityLogger logger;

	private MessMod() {
		this.options = new Options(false);
		this.boxRenderer = new ServerSyncedBoxRenderer();
		this.mapping = this.tryLoadMapping();
		this.logger = new EntityLogger();
	}

	@Override
	public void onInitialize() {
	}
	
	private Mapping tryLoadMapping() {
		try {
			Class.forName("net.minecraft.entity.Entity$827");	// TODO Remove $827
			LOGGER.info("The Minecraft has probably been deobfuscated, the mapping won't be loaded");
			return new DummyMapping();
		} catch (ClassNotFoundException e) {
			File mapping = new File(FabricLoader.getInstance().getGameDir().toString() + "/mappings/" + 
					SharedConstants.getGameVersion().getName() + ".tiny");
			if(mapping.exists()) {
				LOGGER.info("Found corrsponding Tiny mapping, typing to load it...");
				return new TinyMapping(mapping);
			} else {
				LOGGER.error("The mapping couldn't be found, check if the mapping has been downloaded " + 
						"and is in the correct folder. Deobfuscating will be disabled in this running.");
				LOGGER.error("The mapping should be downloaded to" + mapping.getAbsolutePath());
				return new DummyMapping();
			}
		}
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
		Files.copy(MessMod.class.getResourceAsStream("/assets/scarpet/" + name+".sc"), 
				Paths.get(this.scriptDir, name + ".sc"), 
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void onRender(ClientPlayerEntity player, IntegratedServer server) {
		this.updateWindowSize(MinecraftClient.getInstance().getWindow());
		if(this.hudManager != null) this.hudManager.render(player,server);
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
	
	public String getOption(String key) {
		String val = this.options.getProperty(key);
		return val == null ? this.options.getDefault(key) : val;
	}
	
	public void onGameJoined(GameJoinS2CPacket packet) {
		this.hudManager = new HudManager();
	}
	
	public void onServerTicked(MinecraftServer server) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		if(player != null && MessMod.INSTANCE.hudManager != null) {
			MessMod.INSTANCE.hudManager.lookingHud.updateData(player);
		} else if(this.hudManager != null && this.hudManager.playerHudS == null && player != null) {
			this.hudManager.playerHudS = new PlayerHud(this.hudManager, player, true);
		}
		
		if(player != null && this.hudManager != null && this.hudManager.playerHudS != null) this.hudManager.playerHudS.updateData(player);
		this.boxRenderer.tick();
		this.gameTime = this.server.getOverworld().getTime();
		//System.out.println(this.gameTime);
		BlockInfoRenderer fir = new BlockInfoRenderer();
		fir.initializate(this.server);
		fir.tick();
		try {
			this.logger.tick(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onClientTicked() {
		if(this.hudManager != null) {
			this.hudManager.playerHudC.updateData();
		}else {
			this.hudManager = new HudManager();
		}
	}

	public void setOption(String key, String value) {
		this.options.put(key,value);
		this.options.save();
	}

	public void onDisconnected() {
		this.hudManager = null;
	}
	
	public void onPlayerRespawned(PlayerRespawnS2CPacket packet) {
		this.hudManager.playerHudC.refreshPlayer();
	}

	public void onServerStarted(MinecraftServer server) {
		CommandUtil.updateServer(server);
		this.server = server;
		this.boxRenderer.setServer(server);
	}

	public void onServerShutdown(MinecraftServer server) {
		this.boxRenderer.uninitialize();
		this.server = null;
		this.logger.closeAll();
		CommandUtil.updateServer(null);
		this.shapeRenderer.reset();
	}

	public boolean getBooleanOption(String key) {
		return Boolean.parseBoolean((String) this.getOption(key));
	}

	public void onServerPlayerSpawned(ServerPlayerEntity player) {
		System.out.println(player.getEntityName() + " spawned");
		this.hudManager.playerHudS = new PlayerHud(this.hudManager, player, true);
		CommandUtil.tryUpdatePlayer(player);
		try {
			this.scriptDir = server.getSavePath(WorldSavePathMixin.create("scripts")).toAbsolutePath().toString();
			copyScript("tool");
			if(FabricLoader.getInstance().isModLoaded("carpet")) {
				if(this.getBooleanOption("enabledTools")) {
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
		for(Object ob:message) {
			merged += ob;
		}
		this.server.getPlayerManager().broadcastChatMessage(new LiteralText(merged), 
				MessageType.SYSTEM, 
				new UUID(0x31f38bL,0x31f0b8L));
	}

	public String getScriptDir() {
		return scriptDir;
	}

	public ShapeRenderer getShapeRenderer() {
		return this.shapeRenderer;
	}
	
	public EntityLogger getEntityLogger() {
		return this.logger;
	}
}
