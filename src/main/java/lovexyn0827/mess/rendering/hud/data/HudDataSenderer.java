package lovexyn0827.mess.rendering.hud.data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 
 * @author lovexyn0827
 * @date 2022/7/14
 */
public interface HudDataSenderer {
	void updateData(Entity entity);
	List<HudLine> getCustomLines();
	
	/**
	 * @implNote Update the state of the client
	 */
	boolean addLine(HudLine line);
	boolean removeField(String name);
	
	default boolean addField(Class<?> cl, String field) {
		return this.addField(cl, field, field, AccessingPath.DUMMY);
	}
	
	default boolean addField(Class<?> cl, String field, String name, AccessingPath path) {
		Field f = Reflection.getFieldFromNamed(cl, field);
		ListenedField lf = new ListenedField(f, path, name);
		return this.addLine(lf);
	}

	default List<ListenedField> getListenedFields() {
		return this.getCustomLines().stream()
				.filter(ListenedField.class::isInstance)
				.map((l) -> (ListenedField) l)
				.collect(Collectors.toList());
	}
	
	public static HudDataSenderer createHudDataSenderer(HudType type) {
		if(MessMod.INSTANCE.isDedicatedEnv()) {
			// TODO
			throw new AssertionError();
		} else {
			switch(type) {
			case TARGET: 
				return new LocalDataStorage();
			case SERVER_PLAYER: 
				return new LocalPlayerDataStorage(true);
			case CLIENT_PLAYER: 
				return new LocalPlayerDataStorage(false);
			}
			
			throw new AssertionError();
		}
	}
	
	default void updateLookingAtEntityData(ServerPlayerEntity player) {
		this.updateData(getTarget(player));
	}
	
	static Entity getTarget(ServerPlayerEntity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		for(Entity entity : player.world.getEntitiesByClass((Class<? extends Entity>) Entity.class, 
				player.getBoundingBox().expand(direction.x, direction.y, direction.z),  
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
