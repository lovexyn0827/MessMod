package mc.lovexyn0827.mcwmem;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;

public class LookingAtEntityHud extends DrawableHelper {
	public static Entity lastLoogkigAtEntity;
	public static final String[] HEADERS = {
			"X:",
			"Y:",
			"Z:",
			"Vx:",
			"Vy:",
			"Vz:",
			"Yaw:",
			"Pitch:",
			"Forward:",
			"Sideway:",
			"Upwards:",
			"PowerX:",
			"PowerY:",
			"PowerZ:",
			"ID:"
			
	};
	/**
	 * 0-2		Pos
	 * 3-5		Motion
	 * 6		Yaw
	 * 7		Pitch
	 * 8-10		Speed
	 * 11-13	Power
	 * 14		ID
	 */
	public static double[] data = new double[15];
	
	public synchronized static void setData(double val,int index) {
		data[index] = val;
	}
	
	public synchronized static double getData(int index) {
		return data[index];
	}
	
	public static void render(MatrixStack ms, MinecraftClient client) {
		int y = 10;
		int x = 10;
		int i = 0;
		for(double item:data) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			//drawStringWithShadow(ms, client.textRenderer, HEADERS[i]+item, x, y+=10, 16777215);
			client.textRenderer.drawWithShadow(ms, HEADERS[i]+(i==14?(int)item:item), x, y, -1);
			client.textRenderer.getClass();
			y += 10;
			i++;
		}
		//client.getServer().sendSystemMessage(new LiteralText("Log"), UUID.randomUUID());
	}
	
	public static void updateData(PlayerEntity player) {
		Vec3d pos = player.getPos().add(0,player.getStandingEyeHeight(),0);
		Vec3d direction = player.getRotationVector().multiply(10);
		Vec3d max = pos.add(direction);
		Entity target = null;
		double minDistance = 18;
		for(Entity entity:player.world.getEntitiesByClass((Class<? extends Entity>) Entity.class,
				player.getBoundingBox().expand(direction.x,direction.y,direction.z), 
				(e)->true)) {
			Optional<Vec3d> result = entity.getBoundingBox().raycast(pos, max);
			if(result.isPresent()) {
				if(result.get().subtract(pos).length()<minDistance) {
					target = entity;
					max = result.get();
					minDistance = result.get().subtract(pos).length();
				}
			}
		}
		if(target==null) target = player;
		Arrays.fill(data, Double.NaN);
		if(target.getPos().subtract(pos).length()>1E-7) {
			data[0] = target.getX();
			data[1] = target.getY();
			data[2] = target.getZ();
			Vec3d vec = target.getVelocity();
			data[3] = vec.x;
			data[4] = vec.y;
			data[5] = vec.z;
			data[6] = target.yaw;
			data[7] = target.pitch;
			data[14] = target.getEntityId();
			if(target instanceof LivingEntity) {
				LivingEntity living = (LivingEntity)target;
				data[8] = living.forwardSpeed;
				data[9] = living.sidewaysSpeed;
				data[10] = living.upwardSpeed;
			}else if(target instanceof ExplosiveProjectileEntity) {
				ExplosiveProjectileEntity fireball = (ExplosiveProjectileEntity)target;
				data[11] = fireball.posX;
				data[11] = fireball.posY;
				data[11] = fireball.posZ;
			}
		}
	}
	
	static {
		Arrays.fill(data, Double.NaN);
	}
}
