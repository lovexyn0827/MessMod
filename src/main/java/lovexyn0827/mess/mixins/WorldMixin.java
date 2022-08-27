package lovexyn0827.mess.mixins;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.command.FreezeEntityCommand;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin {
	@Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
	private void tickIfNeeded(Consumer<Entity> tickConsumer, Entity entity, CallbackInfo ci) {
		if(FreezeEntityCommand.FROZEN_ENTITIES.contains(entity)) {
			ci.cancel();
		}
	}
}
