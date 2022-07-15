package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.rendering.hud.data.HudDataSenderer;
import lovexyn0827.mess.rendering.hud.data.PlayerHudDataSenderer;

public class ServerHudManager implements HudManager {
	public final HudDataSenderer lookingHud = HudDataSenderer.createHudDataSenderer(HudType.TARGET);
	public final PlayerHudDataSenderer playerHudC = 
			(PlayerHudDataSenderer) HudDataSenderer.createHudDataSenderer(HudType.CLIENT_PLAYER);
	public final PlayerHudDataSenderer playerHudS = 
			(PlayerHudDataSenderer) HudDataSenderer.createHudDataSenderer(HudType.SERVER_PLAYER);
}
