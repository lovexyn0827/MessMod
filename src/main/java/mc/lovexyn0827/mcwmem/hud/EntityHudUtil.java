package mc.lovexyn0827.mcwmem.hud;

import java.util.Arrays;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.util.math.Vec3d;

public class EntityHudUtil {
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
	
	public static int render(MatrixStack ms, MinecraftClient client,double[] data, int yStart,String describe) {
		int y = yStart;
		int x = client.getWindow().getScaledWidth();
		int i = 0;
		TextRenderer tr = client.textRenderer;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		tr.drawWithShadow(ms,describe,x-150,y, -1);
		y += 10;
		for(double item:data) {
			if(Double.isFinite(item)) {
				String line = HEADERS[i]+(i==14?(int)item:item);
				tr.drawWithShadow(ms,line,x-150,y, -1);
				client.textRenderer.getClass();
				y += 10;
				i++;
			}
			
		}
		return y+10;
	}
	
	public static void updateData(Entity entity,double[] data) {
		Arrays.fill(data, Double.NaN);
		if(entity==null) return;
		data[0] = entity.getX();
		data[1] = entity.getY();
		data[2] = entity.getZ();
		Vec3d vec = entity.getVelocity();
		data[3] = vec.x;
		data[4] = vec.y;
		data[5] = vec.z;
		data[6] = entity.yaw;
		data[7] = entity.pitch;
		data[14] = entity.getEntityId();
		if(entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entity;
			data[8] = living.forwardSpeed;
			data[9] = living.sidewaysSpeed;
			data[10] = living.upwardSpeed;
		}else if(entity instanceof ExplosiveProjectileEntity) {
			ExplosiveProjectileEntity fireball = (ExplosiveProjectileEntity)entity;
			data[11] = fireball.posX;
			data[12] = fireball.posY;
			data[13] = fireball.posZ;
		}
	}
}
