package mc.lovexyn0827.mcwmem.hud;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class LookingAtEntityHud extends DrawableHelper {
	public static Entity lastLoogkigAtEntity;
	/**
	 * 0-2		Pos
	 * 3-5		Motion
	 * 6		Yaw
	 * 7		Pitch
	 * 8-10		Speed
	 * 11-13	Power
	 * 14		ID
	 */
	public static double[] data = new double[15];
	public static boolean shouldRender = false;
	
	public static int render(int y) {
		String describe =  "TargetEntity"+(Double.isNaN(data[0])?"(Null)":"");
		return EntityHudUtil.render(new MatrixStack(), MinecraftClient.getInstance(), data,y,describe);
	}
	
	static {
		Arrays.fill(data, Double.NaN);
	}
	
	public static void updateData(ServerPlayerEntity player) {
		EntityHudUtil.updateData(getTarget(player), data);
	}
	
	public static Entity getTarget(ServerPlayerEntity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		for(Entity entity:player.world.getEntitiesByClass((Class<? extends Entity>) Entity.class,
				player.getBoundingBox().expand(direction.x,direction.y,direction.z), 
				(e)->true)) {
			if(entity.getUuid()==player.getUuid()) continue;
			Optional<Vec3d> result = entity.getBoundingBox().raycast(pos, max);
			if(result.isPresent()) {
				if(result.get().subtract(pos).length()<minDistance) {
					target = entity;
					max = result.get();
					minDistance = result.get().subtract(pos).length();
				}
			}
		}
		return target;
	}
}
