package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.scoreboard.AbstractTeam;

@Mixin(EntityPredicates.class)
public class EntityPredicatesMixin {
	@Inject(method = "method_5915", 
			at = @At("HEAD"), 
			cancellable = true, 
			remap = false
	)
	private static void skipEntityIfNeeded(Entity entity, AbstractTeam team, AbstractTeam.CollisionRule rule, 
			Entity entityBeingTicked, CallbackInfoReturnable<Boolean> cir) {
		if(OptionManager.optimizedEntityPushing) {
			if(entity instanceof AbstractMinecartEntity) {
				if(entity.squaredDistanceTo(entityBeingTicked) < 9.999999747378752E-5D) {
					cir.setReturnValue(false);
					cir.cancel();
				}
			} else {
				double dx = entity.getX() - entityBeingTicked.getX();
				double dz = entity.getZ() - entityBeingTicked.getZ();
				if(Math.abs(dx) < 0.009999999776482582D && Math.abs(dz) < 0.009999999776482582D) {
					cir.setReturnValue(false);
					cir.cancel();
				}
			}
		}
	}
}
