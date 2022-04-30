package lovexyn0827.mess.rendering.hud;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.blaze3d.systems.RenderSystem;

import lovexyn0827.mess.mixins.BoatEntityAccessor;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudInfo;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingPath;

import java.util.TreeMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public abstract class EntityHud {
	protected MinecraftClient client = MinecraftClient.getInstance();
	@Deprecated	// Never invoke directly
	private Map<HudInfo,Object> data = new TreeMap<>();
	public boolean shouldRender = false;
	protected int xStart;
	protected int yStart;
	private HudManager hudManager;
	private int lastLineWidth = 0;
	private List<ListenedField> listenedFields = new ArrayList<>();
	
	public EntityHud(HudManager hudManager) {
		this.hudManager = hudManager;
	}
	
	public synchronized void render(MatrixStack ms, String description) {
		int y = this.yStart;
		int x = this.xStart;
		// i don't know how it works, but it runs correctly...
		RenderSystem.matrixMode(5889);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0D, (double)this.client.getWindow().getFramebufferWidth(), (double)this.client.getWindow().getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
		RenderSystem.matrixMode(5888);
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
		float size = OptionManager.hudTextSize;
		RenderSystem.scalef(size, size, size);
		TextRenderer tr = client.textRenderer;
		tr.drawWithShadow(ms,description,x,y, -1);
		y += 10;
		Iterator<Entry<HudInfo, Object>> iterator = this.getData().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<HudInfo, Object> item = iterator.next();
			if(item.getKey() == BuiltinHudInfo.NAME || item.getKey() == BuiltinHudInfo.ID) continue;
			tr.drawWithShadow(ms, item.getKey().toLine(item.getValue()), x, y, 0x31f38b);
			y += 10;
		}
		
		this.updateAlign();
		this.hudManager.hudHeight += y-this.yStart;
	}
	
	public synchronized void updateData(Entity entity) {
		this.getData().clear();
		if (entity == null) return;
		this.getData().put(BuiltinHudInfo.ID, entity.getEntityId());
		String name = entity.hasCustomName() ? entity.getCustomName().asString() : entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "");
		this.getData().put(BuiltinHudInfo.NAME, name);
		this.getData().put(BuiltinHudInfo.AGE, entity.age);
		Vec3d pos = entity.getPos();
		this.getData().put(BuiltinHudInfo.POS_X, pos.x);
		this.getData().put(BuiltinHudInfo.POS_Y, pos.y);
		this.getData().put(BuiltinHudInfo.POS_Z, pos.z);
		Vec3d vec = entity.getVelocity();
		this.getData().put(BuiltinHudInfo.MOTION_X, vec.x);
		this.getData().put(BuiltinHudInfo.MOTION_Y, vec.y);
		this.getData().put(BuiltinHudInfo.MOTION_Z, vec.z);
		this.getData().put(BuiltinHudInfo.DELTA_X, pos.x-entity.prevX);
		this.getData().put(BuiltinHudInfo.DELTA_Y, pos.y-entity.prevY);
		this.getData().put(BuiltinHudInfo.DELTA_Z, pos.z-entity.prevZ);
		this.getData().put(BuiltinHudInfo.YAW, entity.yaw);
		this.getData().put(BuiltinHudInfo.PITCH, entity.pitch);
		this.getData().put(BuiltinHudInfo.FALL_DISTANCE, entity.fallDistance);
		this.getData().put(BuiltinHudInfo.GENERAL_FLAGS, EntityHudUtil.getGeneralFlags(entity));
		this.getData().put(BuiltinHudInfo.POSE, entity.getPose());
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entity;
			this.getData().put(BuiltinHudInfo.HEALTH, living.getHealth());
			this.getData().put(BuiltinHudInfo.FORWARD, living.forwardSpeed);
			this.getData().put(BuiltinHudInfo.SIDEWAYS, living.sidewaysSpeed);
			this.getData().put(BuiltinHudInfo.UPWARD, living.upwardSpeed);
			this.getData().put(BuiltinHudInfo.MOVEMENT_SPEED, living.getMovementSpeed());
			this.getData().put(BuiltinHudInfo.FLYING_SPEED, living.flyingSpeed);
			this.getData().put(BuiltinHudInfo.LIVING_FLAGS, EntityHudUtil.getLivingFlags(living));
		} else if (entity instanceof TntEntity) {
			this.getData().put(BuiltinHudInfo.FUSE, ((TntEntity)entity).getFuseTimer());
		} else if (entity instanceof ExplosiveProjectileEntity) {
			ExplosiveProjectileEntity epe = (ExplosiveProjectileEntity)entity;
			this.getData().put(BuiltinHudInfo.POWER_X, epe.posX);
			this.getData().put(BuiltinHudInfo.POWER_Y, epe.posY);
			this.getData().put(BuiltinHudInfo.POWER_Z, epe.posZ);
		} else if (entity instanceof BoatEntity) {
			this.getData().put(BuiltinHudInfo.VELOCITY_DECAY, ((BoatEntityAccessor)entity).getVelocityDeacyMCWMEM());
		}
		
		this.listenedFields.forEach((f) -> {
			if(f.canGetFrom(entity)) {
				this.getData().put(f, entity);
			}
		});
	}
	
	public void toggleRender() {
		this.shouldRender ^= true;
	}
	
	private void updateAlign() {
		AlignMode mode = OptionManager.hudAlignMode;
		this.lastLineWidth = this.getMaxLineLength();
		float size = OptionManager.hudTextSize;
		this.xStart = mode.name().contains("LEFT") ? 0 : (int) (MinecraftClient.getInstance().getWindow().getWidth() / size - this.lastLineWidth);
		int offset = this.hudManager.hudHeight;
		this.yStart = mode.name().contains("TOP") ? offset : MinecraftClient.getInstance().getWindow().getHeight()- this.getData().size() * 10 - offset;
	}
	
	@SuppressWarnings("resource")
	protected synchronized int getMaxLineLength() {
		int lineLength = 0;
		Iterator<Entry<HudInfo, Object>> iterator = this.getData().entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<HudInfo, Object> item = iterator.next();
			TextRenderer tr = MinecraftClient.getInstance().textRenderer;
			lineLength = Math.max(lineLength, tr.getWidth(item.getKey().toLine(item.getValue())));
		}
		
		return OptionManager.stableHudLocation ? Math.max(lineLength, this.lastLineWidth) : lineLength;
	}

	protected final synchronized Map<HudInfo, Object> getData() {
		return this.data;
	}
	
	public final boolean addField(Class<?> cl, String field) {
		return this.addField(cl, field, field, AccessingPath.DUMMY);
	}
	
	public synchronized final boolean addField(Class<?> cl, String field, String name, AccessingPath path) {
		Field f = Reflection.getFieldFromNamed(cl, field);
		ListenedField lf = new ListenedField(f, path, name);
		if(this.listenedFields.contains(lf)) {
			return false;
		} else {
			this.listenedFields.add(lf);
			return true;
		}
		
	}
	
	public final List<ListenedField> getListenedFields() {
		return new ArrayList<>(this.listenedFields);
	}
}
