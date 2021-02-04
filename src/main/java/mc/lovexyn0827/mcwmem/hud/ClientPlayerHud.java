package mc.lovexyn0827.mcwmem.hud;

import java.util.Arrays;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class ClientPlayerHud {
	public static double[] data = new double[15];
	public static boolean shouldRender;

	public static int render(int y) {
		return EntityHudUtil.render(new MatrixStack(), MinecraftClient.getInstance(), data, y,"Local Player");
	}
	
	public static void updateData() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		EntityHudUtil.updateData(player, data);
	}
	
	static {
		Arrays.fill(data, Double.NaN);
	}
}
