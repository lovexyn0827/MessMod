package lovexyn0827.mess.rendering.hud.data;

import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import lovexyn0827.mess.mixins.BoatEntityAccessor;
import lovexyn0827.mess.rendering.hud.EntityHudUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public enum BuiltinHudInfo implements HudLine {
	ID("ID", DataType.INTEGER, Entity::getId, null),
	NAME("Name", DataType.STRING, (entity) -> {
		return entity.hasCustomName() ? entity.getCustomName().getContent() : entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "");
	}),
	POS_X("Pos X", DataType.DOUBLE, Entity::getX),
	POS_Y("Pos Y", DataType.DOUBLE, Entity::getY),
	POS_Z("Pos Z", DataType.DOUBLE, Entity::getZ),
	AGE("Age", DataType.INTEGER, (e) -> e.age),
	MOTION_X("Motion X", DataType.DOUBLE, (e) -> e.getVelocity().x),
	MOTION_Y("Motion Y", DataType.DOUBLE, (e) -> e.getVelocity().y),
	MOTION_Z("Motion Z", DataType.DOUBLE, (e) -> e.getVelocity().z),
	DELTA_X("Delta X", DataType.DOUBLE, (e) -> e.getX() - e.prevX),
	DELTA_Y("Delta Y", DataType.DOUBLE, (e) -> e.getY() - e.prevY),
	DELTA_Z("Delta Z", DataType.DOUBLE, (e) -> e.getZ() - e.prevZ),
	YAW("Yaw", DataType.FLOAT, (e) -> e.getYaw()),
	PITCH("Pitch", DataType.FLOAT, (e) -> e.getPitch()),
	FALL_DISTANCE("Fall Distance", DataType.FLOAT, (e) -> e.fallDistance),
	GENERAL_FLAGS("State", DataType.STRING, EntityHudUtil::getGeneralFlags),
	FORWARD("Forward", DataType.FLOAT, (e) -> ((LivingEntity) e).forwardSpeed, LivingEntity.class),
	SIDEWAYS("SideWays", DataType.FLOAT, (e) -> ((LivingEntity) e).sidewaysSpeed, LivingEntity.class),
	UPWARD("Upwards", DataType.FLOAT, (e) -> ((LivingEntity) e).upwardSpeed, LivingEntity.class),
	HEALTH("Health", DataType.FLOAT, (e) -> ((LivingEntity) e).getHealth(), LivingEntity.class),
	LIVING_FLAGS("Living State", DataType.STRING, (e) -> EntityHudUtil.getLivingFlags((LivingEntity) e), LivingEntity.class),
	MOVEMENT_SPEED("OnLand Speed", DataType.FLOAT, (e) -> ((LivingEntity) e).getMovementSpeed(), LivingEntity.class),
	//XXX FLYING_SPEED("Fly Speed", DataType.FLOAT, (e) -> ((LivingEntity) e).airStrafingSpeed, LivingEntity.class),
	FUSE("Fuse", DataType.INTEGER, (e) -> ((TntEntity) e).getFuse(), TntEntity.class),
	POWER_X("Power X", DataType.DOUBLE, (e) -> ((ExplosiveProjectileEntity) e).powerX, ExplosiveProjectileEntity.class),
	POWER_Y("Power Y", DataType.DOUBLE, (e) -> ((ExplosiveProjectileEntity) e).powerY, ExplosiveProjectileEntity.class),
	POWER_Z("Power Z", DataType.DOUBLE, (e) -> ((ExplosiveProjectileEntity) e).powerZ, ExplosiveProjectileEntity.class),
	VELOCITY_DECAY("Decay", DataType.FLOAT, (e) -> ((BoatEntityAccessor) e).getVelocityDeacyMCWMEM(), BoatEntity.class),
	POSE("Pose", DataType.POSE, Entity::getPose);
	
	public static final Map<String, BuiltinHudInfo> BY_TITLE = Maps.newHashMap();
	public final String header;
	public final DataType type;
	public final Function<Entity, Object> getter;
	public final Class<? extends Entity> classRequirment;
	
	private BuiltinHudInfo(String header, DataType type, Function<Entity, Object> getter, @Nullable Class<? extends Entity> classRequirment){
		this.header = header;
		this.type = type;
		this.getter = getter;
		this.classRequirment = classRequirment == null ? Entity.class : classRequirment;
	}
	
	private BuiltinHudInfo(String header,DataType type, Function<Entity, Object> getter){
		this(header, type, getter, null);
	}

	@Override
	public String getFrom(Entity in) {
		return this.type.getStringOf(this.getter.apply(in));
	}

	@Override
	public boolean canGetFrom(Entity entity) {
		return this.classRequirment.isInstance(entity);
	}

	@Override
	public String getName() {
		return this.header;
	}
	
	static {
		for(BuiltinHudInfo line : values()){
			BY_TITLE.put(line.header, line);
		}
	}
}
