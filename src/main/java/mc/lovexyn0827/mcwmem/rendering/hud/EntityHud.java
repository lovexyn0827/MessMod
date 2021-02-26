package mc.lovexyn0827.mcwmem.rendering.hud;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.mixins.BoatEntityAccessor;
import mc.lovexyn0827.mcwmem.rendering.hud.data.EntityHudInfoType;
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
		Iterator<Entry<EntityHudInfoType, Object>> iterator = this.getData().entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<EntityHudInfoType, Object> item = iterator.next();
			if(item.getKey()==EntityHudInfoType.NAME||item.getKey()==EntityHudInfoType.ID) continue;
			tr.drawWithShadow(ms, entryToLine(item), x, y, 0x31f38b);
			y+=10;
		}
		this.updateAlign();
		this.hudManager.hudHeight += y-this.yStart;
	}
	
	protected static String entryToLine(Entry<EntityHudInfoType, Object> entry) {
		EntityHudInfoType type = entry.getKey();
		return type.header+type.type.getStringOf(entry.getValue());
	}
	
	public synchronized void updateData(Entity entity) {
		
	this.getData().clear();
		if(entity==null) return;
	this.getData().put(EntityHudInfoType.ID, entity.getEntityId());
		String name = entity.hasCustomName()?entity.getCustomName().asString():entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "");
	this.getData().put(EntityHudInfoType.NAME, name);
	this.getData().put(EntityHudInfoType.AGE, entity.age);
		Vec3d pos = entity.getPos();
	this.getData().put(EntityHudInfoType.POS_X, pos.x);
	this.getData().put(EntityHudInfoType.POS_Y, pos.y);
	this.getData().put(EntityHudInfoType.POS_Z, pos.z);
		Vec3d vec = entity.getVelocity();
	this.getData().put(EntityHudInfoType.MOTION_X, vec.x);
	this.getData().put(EntityHudInfoType.MOTION_Y, vec.y);
	this.getData().put(EntityHudInfoType.MOTION_Z, vec.z);
	this.getData().put(EntityHudInfoType.DELTA_X, pos.x-entity.prevX);
	this.getData().put(EntityHudInfoType.DELTA_Y, pos.y-entity.prevY);
	this.getData().put(EntityHudInfoType.DELTA_Z, pos.z-entity.prevZ);
	this.getData().put(EntityHudInfoType.YAW, entity.yaw);
	this.getData().put(EntityHudInfoType.PITCH, entity.pitch);
	this.getData().put(EntityHudInfoType.FALL_DISTANCE, entity.fallDistance);
	this.getData().put(EntityHudInfoType.GENERAL_FLAGS, EntityHudUtil.getGeneralFlags(entity));
		if(entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entity;
		this.getData().put(EntityHudInfoType.HEALTH, living.getHealth());
		this.getData().put(EntityHudInfoType.FORWARD, living.forwardSpeed);
		this.getData().put(EntityHudInfoType.SIDEWAYS, living.sidewaysSpeed);
		this.getData().put(EntityHudInfoType.UPWARD, living.upwardSpeed);
		this.getData().put(EntityHudInfoType.MOVEMENT_SPEED, living.getMovementSpeed());
		this.getData().put(EntityHudInfoType.FLYING_SPEED, living.flyingSpeed);
		this.getData().put(EntityHudInfoType.LIVING_FLAGS, EntityHudUtil.getLivingFlags(living));
		}else if(entity instanceof TntEntity) {
		this.getData().put(EntityHudInfoType.FUSE, ((TntEntity)entity).getFuse());
		}else if(entity instanceof ExplosiveProjectileEntity) {
			ExplosiveProjectileEntity epe = (ExplosiveProjectileEntity)entity;
		this.getData().put(EntityHudInfoType.POWER_X, epe.posX);
		this.getData().put(EntityHudInfoType.POWER_Y, epe.posY);
		this.getData().put(EntityHudInfoType.POWER_Z, epe.posZ);
		}else if(entity instanceof BoatEntity) {
		this.getData().put(EntityHudInfoType.VELOCITY_DECAY, ((BoatEntityAccessor)entity).getVelocityDeacyMCWMEM());
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
		this.yStart = mode.name().contains("TOP")?offset:MCWMEMod.INSTANCE.getWindowY()-this.getData().size()*10-offset;
	}
	
	@SuppressWarnings("resource")
	protected synchronized int getMaxLineLength() {
		int lineLength = 0;
		Iterator<Entry<EntityHudInfoType, Object>> iterator = this.getData().entrySet().iterator();
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
