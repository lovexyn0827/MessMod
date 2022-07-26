package lovexyn0827.mess.rendering.hud.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import lovexyn0827.mess.mixins.BoatEntityAccessor;
import lovexyn0827.mess.rendering.hud.EntityHudUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class LocalDataStorage implements HudDataSenderer, HudDataStorage {
	private Map<HudLine, Object> data = new TreeMap<>();
	private List<HudLine> customLines = new ArrayList<>();

	@Override
	public synchronized void updateData(Entity entity) {
		this.data.clear();
		if (entity == null) return;
		this.data.put(BuiltinHudInfo.ID, entity.getEntityId());
		String name = entity.hasCustomName() ? entity.getCustomName().asString() : entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "");
		this.data.put(BuiltinHudInfo.NAME, name);
		this.data.put(BuiltinHudInfo.AGE, entity.age);
		Vec3d pos = entity.getPos();
		this.data.put(BuiltinHudInfo.POS_X, pos.x);
		this.data.put(BuiltinHudInfo.POS_Y, pos.y);
		this.data.put(BuiltinHudInfo.POS_Z, pos.z);
		Vec3d vec = entity.getVelocity();
		this.data.put(BuiltinHudInfo.MOTION_X, vec.x);
		this.data.put(BuiltinHudInfo.MOTION_Y, vec.y);
		this.data.put(BuiltinHudInfo.MOTION_Z, vec.z);
		this.data.put(BuiltinHudInfo.DELTA_X, pos.x-entity.prevX);
		this.data.put(BuiltinHudInfo.DELTA_Y, pos.y-entity.prevY);
		this.data.put(BuiltinHudInfo.DELTA_Z, pos.z-entity.prevZ);
		this.data.put(BuiltinHudInfo.YAW, entity.yaw);
		this.data.put(BuiltinHudInfo.PITCH, entity.pitch);
		this.data.put(BuiltinHudInfo.FALL_DISTANCE, entity.fallDistance);
		this.data.put(BuiltinHudInfo.GENERAL_FLAGS, EntityHudUtil.getGeneralFlags(entity));
		this.data.put(BuiltinHudInfo.POSE, entity.getPose());
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entity;
			this.data.put(BuiltinHudInfo.HEALTH, living.getHealth());
			this.data.put(BuiltinHudInfo.FORWARD, living.forwardSpeed);
			this.data.put(BuiltinHudInfo.SIDEWAYS, living.sidewaysSpeed);
			this.data.put(BuiltinHudInfo.UPWARD, living.upwardSpeed);
			this.data.put(BuiltinHudInfo.MOVEMENT_SPEED, living.getMovementSpeed());
			this.data.put(BuiltinHudInfo.FLYING_SPEED, living.flyingSpeed);
			this.data.put(BuiltinHudInfo.LIVING_FLAGS, EntityHudUtil.getLivingFlags(living));
		} else if (entity instanceof TntEntity) {
			this.data.put(BuiltinHudInfo.FUSE, ((TntEntity)entity).getFuseTimer());
		} else if (entity instanceof ExplosiveProjectileEntity) {
			ExplosiveProjectileEntity epe = (ExplosiveProjectileEntity)entity;
			this.data.put(BuiltinHudInfo.POWER_X, epe.posX);
			this.data.put(BuiltinHudInfo.POWER_Y, epe.posY);
			this.data.put(BuiltinHudInfo.POWER_Z, epe.posZ);
		} else if (entity instanceof BoatEntity) {
			this.data.put(BuiltinHudInfo.VELOCITY_DECAY, ((BoatEntityAccessor)entity).getVelocityDeacyMCWMEM());
		}
		
		this.customLines.forEach((f) -> {
			if(f.canGetFrom(entity)) {
				this.data.put(f, f.toLine(entity));
			}
		});
	}

	@Override
	public List<HudLine> getCustomLines() {
		return this.customLines;
	}

	@Override
	public boolean addLine(HudLine line) {
		if(this.customLines.contains(line)) {
			return false;
		} else {
			this.customLines.add(line);
			return true;
		}
	}

	@Override
	public boolean removeField(String name) {
		return this.customLines.removeIf(((line) -> line.getName().equals(name)));
	}

	@Override
	@Deprecated
	public void pushData(String name, Object data) {
	}

	@Override
	public synchronized void forEach(BiConsumer<String, Object> action) {
		this.data.forEach((l, d) -> {
			action.accept(l.getName(), d);
		});
	}

	@Override
	@Deprecated
	public Iterator<Entry<HudLine, Object>> iterator() {
		return this.data.entrySet().iterator();
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public synchronized Object get(HudLine id) {
		return this.data.get(id);
	}
}
