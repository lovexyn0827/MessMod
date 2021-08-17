package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;

public class HudManager {
	public LookingAtEntityHud lookingHud = null;
	public PlayerHud playerHudC = null;
	public AlignMode hudAlign = AlignMode.TOP_RIGHT;
	public int hudHeight;
	public PlayerHud playerHudS;
	
	@SuppressWarnings("resource")
	public HudManager() {
		this.hudHeight = 0;
		this.lookingHud = new LookingAtEntityHud(this);
		this.playerHudC = new PlayerHud(this,MinecraftClient.getInstance().player, false);
		this.hudAlign = AlignMode.fromString(MessMod.INSTANCE.getOption("alignMode"));
	}
	
	public void render(ClientPlayerEntity player, IntegratedServer server) {
		this.hudHeight = 0;
		if(this.lookingHud.shouldRender) this.lookingHud.render();
		if(player!=null&&this.playerHudC.shouldRender) {
			this.playerHudC.render();
		}
		if(player!=null && this.playerHudS != null && this.playerHudS.shouldRender) {
			this.playerHudS.render();
		}
	}
	
	public enum AlignMode {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTIM_LEFT,
		BOTTOM_RIGHT;
		
		public static AlignMode fromString(String str) {
			if(str.contains("topLeft")) {
				return HudManager.AlignMode.TOP_LEFT;
			}else if(str.contains("topRight")) {
				return HudManager.AlignMode.TOP_RIGHT;
			}else if(str.contains("bottomLeft")) {
				return HudManager.AlignMode.BOTTIM_LEFT;
			}else if(str.contains("bottomRight")) {
				return HudManager.AlignMode.BOTTOM_RIGHT;
			}else {
				return null;
			}
		}
	}
}
