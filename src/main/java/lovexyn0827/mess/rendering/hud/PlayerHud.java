package lovexyn0827.mess.rendering.hud;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerHud extends EntityHud{
	private PlayerEntity player;
	private final boolean isServer;

	public PlayerHud(ClientHudManager clientHudManager, PlayerEntity player, boolean isServer) {
		super(clientHudManager, isServer ? HudType.SERVER_PLAYER : HudType.CLIENT_PLAYER);
		this.player = player;
		this.isServer = isServer;
	}
	
	public void render() {
		this.render((this.isServer ? "Server" : "Client") + "Player(" + this.player.getId() + ")");
	}
}
