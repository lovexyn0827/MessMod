package lovexyn0827.mess.rendering.hud.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class LocalPlayerDataStorage extends LocalDataStorage implements PlayerHudDataSender {
	private PlayerEntity player;
	/** Server player information HUD */
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
