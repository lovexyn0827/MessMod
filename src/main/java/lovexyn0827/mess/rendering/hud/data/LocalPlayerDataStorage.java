package lovexyn0827.mess.rendering.hud.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class LocalPlayerDataStorage extends LocalDataStorage implements PlayerHudDataSenderer {
	private PlayerEntity player;
	private final boolean isServer;

	public LocalPlayerDataStorage(boolean isServer) {
		this.isServer = isServer;
	}

	@Override
	public void updateData() {
		if(this.isServer) {
			throw new IllegalStateException();
		}
		
		this.updateData(this.player);
	}
	
	@Override
	@SuppressWarnings("resource")
	public void updatePlayer() {
		if(this.isServer) return;
		this.player = MinecraftClient.getInstance().player;
	}
}
