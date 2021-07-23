package mc.lovexyn0827.mcwmem.rendering.hud;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class EntityHudUtil {
	public static String getLivingFlags(LivingEntity living) {
		String result = "|";
		if(living.hurtTime==living.maxHurtTime-1) result += "Hurt|";
		if(living.isFallFlying()) result += "Fly|";
		if(living.isSleeping()) result += "Slp|";
		if(living.isDead()) result += "Dead|";
		return result;
	}

	public static String getGeneralFlags(Entity entity) {
		String result = "|";
		if(entity.isGlowing()) result += "Gl|";
		if(entity.isInvulnerable()) result += "Inv|";
		if(entity.isCollidable()) result += "Col";
		if(entity.hasNoGravity()) result += "NG|";
		if(entity.horizontalCollision) result += "HC|";
		if(entity.verticalCollision) result += "VC|";
		if(entity.isWet()) result += "Wet|";
		if(entity.isSprinting()) result += "Sp|";
		if(entity.isSneaking()) result += "Sn|";
		if(entity.isDescending()) result += "De|";
		if(entity.isSwimming()) result += "Sw|";
		if(entity.isOnGround()) result += "Og|";
		return result;
	}
}
