package mc.lovexyn0827.mcwmem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import mc.lovexyn0827.mcwmem.command.CommandUtil;
import mc.lovexyn0827.mcwmem.mixins.WorldSavePathMixin;
import mc.lovexyn0827.mcwmem.rendering.ExplosionRenderer;
import mc.lovexyn0827.mcwmem.rendering.ServerSyncedBoxRenderer;
import mc.lovexyn0827.mcwmem.rendering.hud.HudManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
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
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

public class MCWMEMod implements ModInitializer {
	public static final MCWMEMod INSTANCE = new MCWMEMod();
	public final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
	public HudManager hudManager;
	private Options options;
	private int windowX;
	private int windowY;
	private ServerSyncedBoxRenderer boxRenderer;
	private ExplosionRenderer explosionRenderer;
	private MinecraftServer server;
	private String scriptDir;

	private MCWMEMod() {
		this.options = new Options(false);
		this.boxRenderer = new ServerSyncedBoxRenderer();
	}
	
	@Override
	public void onInitialize() {
	}
	
	private void copyScript(String name) throws IOException {
		Path scriptPath = Paths.get(this.scriptDir);
		if(!Files.exists(scriptPath)) {
			Files.createDirectories(scriptPath);
		}
		Files.copy(MCWMEMod.class.getResourceAsStream("/assets/scarpet/"+name+".sc"), 
				Paths.get(this.scriptDir, name+".sc"), 
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void onRender(ClientPlayerEntity player, IntegratedServer server) {
		this.updateWindowSize(MinecraftClient.getInstance().getWindow());
		if(this.hudManager!=null) this.hudManager.render(player,server);
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
		this.options.load();
		String val = this.options.getProperty(key);
		return val==null?this.options.getDefault(key):val;
	}
	
	public void onGameJoined(GameJoinS2CPacket packet) {
		this.hudManager = new HudManager();
	}
	
	public void onServerTicked(MinecraftServer server) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		if(player!=null&&MCWMEMod.INSTANCE.hudManager!=null) MCWMEMod.INSTANCE.hudManager.lookingHud.updateData(player);
		this.boxRenderer.tick();
	}

	public void onClientTicked() {
		if(this.hudManager!=null) {
			this.hudManager.playerHud.updateData();
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
		this.hudManager.playerHud.refreshPlayer();
	}

	public void onServerStarted(MinecraftServer server) {
		CommandUtil.updateServer(server);
		this.server = server;
		this.boxRenderer.setServerAndInitialize(server);
		this.explosionRenderer = new ExplosionRenderer(server);
	}

	public void onServerShutdown(MinecraftServer server) {
		this.boxRenderer.uninitialize();
		this.explosionRenderer = null;
		this.server = null;
		CommandUtil.updateServer(null);
	}
	
	public ExplosionRenderer getExplosionRenderer() {
		return this.explosionRenderer;
	}

	public boolean getBooleanOption(String key) {
		return Boolean.parseBoolean((String) this.getOption(key));
	}

	public void onServerPlayerSpawned(ServerPlayerEntity player) {
		CommandUtil.tryUpdatePlayer(player);
		try {
			this.scriptDir = server.getSavePath(WorldSavePathMixin.create("scripts")).toAbsolutePath().toString();
			copyScript("tool");
			copyScript("server_box");
			if(FabricLoader.getInstance().isModLoaded("carpet")) {
				if(this.getBooleanOption("enabledTools")) {
					this.server.getCommandManager().execute(CommandUtil.noreplyPlayerSources(), 
							"/script load tool");
				}
				if(this.getBooleanOption("serverSyncedBox")) {
					this.server.getCommandManager().execute(CommandUtil.noreplyPlayerSources(), 
							"/script load server_box global");
				}
			}
		} catch (IOException e) {
			CrashReport cr = CrashReport.create(e, "Couldn't load scarpet scripts");
			throw new CrashException(cr);
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
}
