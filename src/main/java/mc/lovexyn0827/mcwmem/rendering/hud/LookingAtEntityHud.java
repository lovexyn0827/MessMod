package mc.lovexyn0827.mcwmem.rendering.hud;

import java.util.Optional;

import mc.lovexyn0827.mcwmem.rendering.hud.data.EntityHudInfoType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class LookingAtEntityHud extends EntityHud {
	public Entity lastLoogkigAtEntity;
	
	public LookingAtEntityHud(HudManager hudManager) {
		super(hudManager);
	}
	
	public void render() {
		String entityInfo = "(Null)";
		if(getData().get(EntityHudInfoType.ID)!=null) {
			entityInfo = "("+getData().get(EntityHudInfoType.ID)+","+
					getData().get(EntityHudInfoType.NAME)+","+
					getData().get(EntityHudInfoType.AGE)+")";
		}
		String describe =  "Target"+entityInfo;
		if(this.shouldRender) this.render(new MatrixStack(), describe);
	}
	
	public void updateData(ServerPlayerEntity player) {
		this.updateData(getTarget(player));
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
