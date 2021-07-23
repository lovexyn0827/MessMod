package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.KillCommand;

@Mixin(KillCommand.class)
public abstract class KillCommandMixin {
	@Redirect(method = "execute", 
			at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/entity/Entity;kill()V")
	)
	private static void removeEntity(Entity entity) {
		if(MCWMEMod.INSTANCE.getBooleanOption("mobFastKill") && entity instanceof MobEntity) {
			entity.remove();
		} else {
			entity.kill();
		}
	}
}
