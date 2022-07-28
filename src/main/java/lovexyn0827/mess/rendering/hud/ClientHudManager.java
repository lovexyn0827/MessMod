package lovexyn0827.mess.rendering.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;

public class ClientHudManager implements HudManager {
	public final LookingAtEntityHud lookingHud;
	public final PlayerHud playerHudC;
	public int hudHeight;
	public PlayerHud playerHudS;
	
	@SuppressWarnings("resource")
	public ClientHudManager() {
		this.hudHeight = 0;
		this.lookingHud = new LookingAtEntityHud(this, HudType.TARGET);
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		this.playerHudC = new PlayerHud(this, player, false);
	}
	
	public void render(ClientPlayerEntity player, IntegratedServer server) {
		this.hudHeight = 0;
		if(this.lookingHud.shouldRender) {
			this.lookingHud.render();
		}
		
		if(player != null && this.playerHudC.shouldRender) {
			this.playerHudC.render();
		}
		
		if(player != null && this.playerHudS != null && this.playerHudS.shouldRender) {
			this.playerHudS.render();
		}
	}
}
