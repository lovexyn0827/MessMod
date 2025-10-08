package lovexyn0827.mess.util;

import java.util.Optional;

import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;

public final class RaycastUtil {
	public static Entity getTargetEntity(Entity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		EntityView world = OptionManager.directChunkAccessForMessMod && player.world instanceof ServerWorld ? 
				((ServerWorldInterface) player.world).toNoChunkLoadingWorld() : player.world;
		for(Entity entity : world.getEntitiesByClass((Class<? extends Entity>) Entity.class, 
				player.getBoundingBox().expand(10),  
				(e) -> e != player)) {
			if(entity.getId() == player.getId()) {
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
