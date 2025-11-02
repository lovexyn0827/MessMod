package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.command.LogDeathCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin({ 
		ItemEntity.class, ItemFrameEntity.class, VehicleEntity.class, 
		ShulkerBulletEntity.class, ExperienceOrbEntity.class, EndCrystalEntity.class, TntMinecartEntity.class, 
		LivingEntity.class
})
public class DieableEntityMixin {
	@SuppressWarnings("resource")
	@Inject(method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z", 
			at = {
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/ItemEntity.discard()V"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/ExperienceOrbEntity.discard()V"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/vehicle/VehicleEntity.discard()V"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/decoration/ItemFrameEntity.dropHeldStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Z)V"
					),
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/decoration/EndCrystalEntity.remove(Lnet/minecraft/entity/Entity$RemovalReason;)V"
					),
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/projectile/ShulkerBulletEntity.destroy()V"
					),  
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/vehicle/AbstractMinecartEntity.dropItems(Lnet/minecraft/entity/damage/DamageSource;)V"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/vehicle/TntMinecartEntity.explode(Lnet/minecraft/entity/damage/DamageSource;D)V"
					), 
					@At(
							value = "INVOKE", 
							target = "net/minecraft/entity/LivingEntity.onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"
					)
			}, 
			require = 1
	)
	private void onDeath(ServerWorld world, DamageSource damage, float amount, CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity)(Object) this;
		if (self.getWorld().isClient) {
			return;
		}
		
		LogDeathCommand.onEntityDies(damage, self, amount);
	}
}
