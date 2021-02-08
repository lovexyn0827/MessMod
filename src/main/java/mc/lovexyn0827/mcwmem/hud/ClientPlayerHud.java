package mc.lovexyn0827.mcwmem.hud;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class ClientPlayerHud extends EntityHud{
	private ClientPlayerEntity player;

	public ClientPlayerHud(HudManager hudManager, ClientPlayerEntity player) {
		super(hudManager);
		this.player = player;
	}
	
	public void render() {
		this.render(new MatrixStack(), "Local Player");
	}
	
	public void updateData() {
		updateData(player);
	}
}
