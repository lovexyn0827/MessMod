package mc.lovexyn0827.mcwmem.hud;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.hud.data.EntityHudInfoType;
import mc.lovexyn0827.mcwmem.mixins.BoatEntityMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class EntityHud {
	protected MinecraftClient client = MinecraftClient.getInstance();
	private Map<EntityHudInfoType,Object> data = new TreeMap<>();
	public boolean shouldRender = false;
	protected int xStart;
	protected int yStart;
	private HudManager hudManager;
	private int lastLineWidth = 0;
	
	public EntityHud(HudManager hudManager) {
		this.hudManager = hudManager;
	}
	
	public synchronized void render(MatrixStack ms, String describe) {
		int y = this.yStart;
		int x = this.xStart;
		TextRenderer tr = client.textRenderer;
		tr.drawWithShadow(ms,describe,x,y, -1);
		y += 10;
		Iterator<Entry<EntityHudInfoType, Object>> iterator = getData().entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<EntityHudInfoType, Object> item = iterator.next();
			if(item.getKey()==EntityHudInfoType.NAME||item.getKey()==EntityHudInfoType.ID) continue;
			tr.drawWithShadow(ms, entryToLine(item), x, y, 20050827);
			y+=10;
		}
		updateAlign();
		this.hudManager.hudHeight += y-this.yStart;
	}
	
	protected static String entryToLine(Entry<EntityHudInfoType, Object> entry) {
		EntityHudInfoType type = entry.getKey();
		return type.header+type.type.getStringOf(entry.getValue());
	}
	
	public synchronized void updateData(Entity entity) {
		getData().clear();
		if(entity==null) return;
		getData().put(EntityHudInfoType.ID, entity.getEntityId());
		String name = entity.hasCustomName()?entity.getCustomName().asString():entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "");
		getData().put(EntityHudInfoType.NAME, name);
		getData().put(EntityHudInfoType.AGE, entity.age);
		Vec3d pos = entity.getPos();
		getData().put(EntityHudInfoType.POS_X, pos.x);
		getData().put(EntityHudInfoType.POS_Y, pos.y);
		getData().put(EntityHudInfoType.POS_Z, pos.z);
		Vec3d vec = entity.getVelocity();
		getData().put(EntityHudInfoType.MOTION_X, vec.x);
		getData().put(EntityHudInfoType.MOTION_Y, vec.y);
		getData().put(EntityHudInfoType.MOTION_Z, vec.z);
		getData().put(EntityHudInfoType.DELTA_X, pos.x-entity.prevX);
		getData().put(EntityHudInfoType.DELTA_Y, pos.y-entity.prevY);
		getData().put(EntityHudInfoType.DELTA_Z, pos.z-entity.prevZ);
		getData().put(EntityHudInfoType.YAW, entity.yaw);
		getData().put(EntityHudInfoType.PITCH, entity.pitch);
		getData().put(EntityHudInfoType.FALL_DISTANCE, entity.fallDistance);
		getData().put(EntityHudInfoType.GENERAL_FLAGS, EntityHudUtil.getGeneralFlags(entity));
		if(entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entity;
			getData().put(EntityHudInfoType.HEALTH, living.getHealth());
			getData().put(EntityHudInfoType.FORWARD, living.forwardSpeed);
			getData().put(EntityHudInfoType.SIDEWAYS, living.sidewaysSpeed);
			getData().put(EntityHudInfoType.UPWARD,living.upwardSpeed);
			getData().put(EntityHudInfoType.MOVEMENT_SPEED, living.getMovementSpeed());
			getData().put(EntityHudInfoType.FLYING_SPEED, living.flyingSpeed);
			getData().put(EntityHudInfoType.LIVING_FLAGS, EntityHudUtil.getLivingFlags(living));
		}else if(entity instanceof TntEntity) {
			getData().put(EntityHudInfoType.FUSE,((TntEntity)entity).getFuse());
		}else if(entity instanceof ExplosiveProjectileEntity) {
			ExplosiveProjectileEntity epe = (ExplosiveProjectileEntity)entity;
			getData().put(EntityHudInfoType.POWER_X, epe.posX);
			getData().put(EntityHudInfoType.POWER_Y, epe.posY);
			getData().put(EntityHudInfoType.POWER_Z, epe.posZ);
		}else if(entity instanceof BoatEntity) {
			getData().put(EntityHudInfoType.VELOCITY_DECAY, ((BoatEntityMixin)entity).getVelocityDeacyMCWMEM());
		}
	}
	
	public void toggleRender() {
		this.shouldRender ^= true;
	}
	
	private void updateAlign() {
		HudManager.AlignMode mode = this.hudManager.hudAlign;
		this.lastLineWidth = getMaxLineLength();
		this.xStart = mode.name().contains("LEFT")?0:MCWMEMod.INSTANCE.getWindowX()-this.lastLineWidth;
		int offset = this.hudManager.hudHeight;
		this.yStart = mode.name().contains("TOP")?offset:MCWMEMod.INSTANCE.getWindowY()-getData().size()*10-offset;
	}
	
	protected synchronized int getMaxLineLength() {
		int lineLength = 0;
		Iterator<Entry<EntityHudInfoType, Object>> iterator = getData().entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<EntityHudInfoType, Object> item = iterator.next();
			TextRenderer tr = MinecraftClient.getInstance().textRenderer;
			lineLength = Math.max(lineLength, tr.getWidth(entryToLine(item)));
		}
		return Math.max(lineLength,this.lastLineWidth );
	}

	protected synchronized Map<EntityHudInfoType, Object> getData() {
		return this.data;
	}
}
