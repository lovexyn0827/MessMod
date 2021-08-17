package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.MessMod;
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
		if(MessMod.INSTANCE.getBooleanOption("mobFastKill") && entity instanceof MobEntity) {
			entity.remove();
		} else {
			entity.kill();
		}
	}
}
