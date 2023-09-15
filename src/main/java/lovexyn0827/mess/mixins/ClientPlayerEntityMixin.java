package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

@Mixin(value = ClientPlayerEntity.class, priority = 900)
public abstract class ClientPlayerEntityMixin {
	@Redirect(method = "tickMovement", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/entity/player/PlayerAbilities;getFlySpeed()F"
			))
	private float modifySpeed(PlayerAbilities abilities) {
		float speed = OptionManager.creativeUpwardsSpeed;
		if(abilities.creativeMode) {
			return Float.isFinite(speed) ? speed : abilities.getFlySpeed();
		} else {
			return abilities.getFlySpeed();
		}
	}
	
}
