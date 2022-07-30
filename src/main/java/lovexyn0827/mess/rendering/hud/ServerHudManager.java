package lovexyn0827.mess.rendering.hud;

import java.util.List;

import com.mojang.authlib.GameProfile;

import lovexyn0827.mess.rendering.hud.data.HudDataSender;
import lovexyn0827.mess.rendering.hud.data.PlayerHudDataSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerHudManager implements HudManager {
	public final HudDataSender lookingHud;
	public final PlayerHudDataSender playerHudC;
	public final PlayerHudDataSender playerHudS;
	private GameProfile subscribedProfile;
	
	public ServerHudManager(MinecraftServer server) {
		lookingHud = HudDataSender.createHudDataSenderer(HudType.TARGET, server);
		playerHudC = (PlayerHudDataSender) HudDataSender.createHudDataSenderer(HudType.CLIENT_PLAYER, server);
		playerHudS = (PlayerHudDataSender) HudDataSender.createHudDataSenderer(HudType.SERVER_PLAYER, server);
	}

	public void setServerPlayerHudTarget(GameProfile gameProfile) {
		this.subscribedProfile = gameProfile;
	}

	public GameProfile getServerPlayerHudTarget() {
		return subscribedProfile;
	}

	public void tick(MinecraftServer server) {
		List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
		if(!players.isEmpty() && this.getServerPlayerHudTarget() == null) {
			this.setServerPlayerHudTarget(players.get(0).getGameProfile());
		}
		
		if (this.getServerPlayerHudTarget() != null) {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(this.getServerPlayerHudTarget().getId());
			if(player != null && this.lookingHud != null) {
				this.lookingHud.updateLookingAtEntityData(player);
			}
			
			if(player != null && this.playerHudS != null) {
				this.playerHudS.updateData(player);
			}
		}
	}
}
