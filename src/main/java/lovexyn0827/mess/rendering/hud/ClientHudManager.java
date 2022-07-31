package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;

public class ClientHudManager implements HudManager {
	public final LookingAtEntityHud lookingHud;
	public final PlayerHud playerHudC;
	public int hudHeight;
	public PlayerHud playerHudS;
	public EntitySideBar sidebar;
	// Whether or not headers of lines are rendered red
	boolean headerSpeciallyColored;
	boolean looserLines;
	boolean renderBackGround;
	
	@SuppressWarnings("resource")
	public ClientHudManager() {
		this.hudHeight = 0;
		this.lookingHud = new LookingAtEntityHud(this);
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		this.playerHudC = new PlayerHud(this, player, false);
		this.sidebar = new EntitySideBar(this);
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
		
		if(player != null && this.sidebar != null && this.sidebar.shouldRender) {
			this.sidebar.render();
		}
	}

	public void updateStyle(String code) {
		this.headerSpeciallyColored = code.contains("R");
		this.looserLines = code.contains("L");
		this.renderBackGround = code.contains("B");
	}
	
	public HudDataStorage getData(HudType type) {
		EntityHud hud;
		switch(type) {
		case TARGET : 
			hud = this.lookingHud;
			break;
		case CLIENT_PLAYER : 
			hud = this.playerHudC;
			break;
		case SERVER_PLAYER : 
			hud = this.playerHudS;
			break;
		case SIDEBAR : 
			hud = this.sidebar;
			break;
		default : 
			throw new IllegalArgumentException();
		}
		
		return hud != null ? hud.getData() : null;
	}
}
