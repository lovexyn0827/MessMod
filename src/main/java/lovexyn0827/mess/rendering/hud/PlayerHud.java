package lovexyn0827.mess.rendering.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerHud extends EntityHud{
	private PlayerEntity player;
	private final boolean isServer;

	public PlayerHud(HudManager hudManager, PlayerEntity player, boolean isServer) {
		super(hudManager);
		this.player = player;
		this.isServer = isServer;
	}
	
	public void render() {
		this.render(new MatrixStack(), (this.isServer ? "Server" : "Client") + "Player(" + this.player.getEntityId() + ")");
	}
	
	public void updateData() {
		updateData(this.player);
	}
	
	@SuppressWarnings("resource")
	public void refreshPlayer() {
		if(this.isServer) return;
		this.player = MinecraftClient.getInstance().player;
	}
}
