package mc.lovexyn0827.mcwmem;

import mc.lovexyn0827.mcwmem.hud.HudManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;

public class MCWMEMod implements ModInitializer {
	public static final MCWMEMod INSTANCE = new MCWMEMod();
	public HudManager hudManager;
	private Options options;
	private int windowX;
	private int windowY;

	private MCWMEMod() {
		this.options = new Options(false);
	}
	
	@Override
	public void onInitialize() {
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
		return this.options.getProperty(key);
	}
	
	public void onGameJoined(GameJoinS2CPacket packet) {
		this.hudManager = new HudManager();
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
}
