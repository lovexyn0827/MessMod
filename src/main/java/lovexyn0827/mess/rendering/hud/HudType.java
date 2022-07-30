package lovexyn0827.mess.rendering.hud;

public enum HudType {
	TARGET, 
	SERVER_PLAYER, 
	CLIENT_PLAYER;
	
	public boolean isPlayer() {
		return this == SERVER_PLAYER || this == CLIENT_PLAYER;
	}
}
