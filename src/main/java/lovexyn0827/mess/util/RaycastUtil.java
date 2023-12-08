package lovexyn0827.mess.util;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class RaycastUtil {
	public static Entity getTargetEntity(Entity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		for(Entity entity : player.world.getEntitiesByClass((Class<? extends Entity>) Entity.class, 
				player.getBoundingBox().expand(10),  
				(e) -> e != player)) {
			if(entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			
			Optional<Vec3d> result = entity.getBoundingBox().raycast(pos, max);
			if(result.isPresent()) {
				if(result.get().subtract(pos).length() < minDistance) {
					target = entity;
					max = result.get();
					minDistance = result.get().subtract(pos).length();
				}
			}
		}
		
		return target;
	}

}
