package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;

public class ClientHudManager implements HudManager {
	public final LookingAtEntityHud lookingHud;
	public final PlayerHud playerHudC;
	public int hudHeight;
	public PlayerHud playerHudS;
	// Whether or not headers of lines are rendered red
	boolean headerSpeciallyColored;
	boolean looserLines;
	boolean renderBackGround;
	
	@SuppressWarnings("resource")
	public ClientHudManager() {
		this.hudHeight = 0;
		this.lookingHud = new LookingAtEntityHud(this, HudType.TARGET);
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		this.playerHudC = new PlayerHud(this, player, false);
		this.updateStyle(OptionManager.hudStyles);
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

	public void updateStyle(String code) {
		this.headerSpeciallyColored = code.contains("R");
		this.looserLines = code.contains("L");
		this.renderBackGround = code.contains("B");
	}
}
