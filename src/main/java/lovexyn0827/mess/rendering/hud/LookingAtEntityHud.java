package lovexyn0827.mess.rendering.hud;

import java.util.Optional;

import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class LookingAtEntityHud extends EntityHud {
	public Entity lastLookingAtEntity;
	
	public LookingAtEntityHud(ClientHudManager clientHudManager) {
		super(clientHudManager, HudType.TARGET);
	}
	
	public void render() {
		String entityInfo;
		if(this.getData().get(BuiltinHudInfo.ID) != null) {
			entityInfo = "("+getData().get(BuiltinHudInfo.ID) + "," + 
					getData().get(BuiltinHudInfo.NAME) + "," + 
					getData().get(BuiltinHudInfo.AGE) + ")";
		} else {
			entityInfo = "[Null]";
		}
		
		String describe = "Target" + entityInfo;
		if(this.shouldRender) this.render(new MatrixStack(), describe);
	}

	public static Entity getTarget(ServerPlayerEntity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		for(Entity entity : player.world.getEntitiesByClass((Class<? extends Entity>) Entity.class, 
				player.getBoundingBox().expand(10),  
				(e) -> true)) {
			if(entity.getUuid() == player.getUuid()) continue;
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
