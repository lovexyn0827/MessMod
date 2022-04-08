package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
	
	private static float speed = Float.NaN;
	
	@Redirect(method = "tickMovement", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/entity/player/PlayerAbilities;getFlySpeed()F"
			))
	private float modifySpeed(PlayerAbilities abilities) {
		try {
			speed = OptionManager.creativeUpwardsSpeed;
			if(Float.isFinite(speed) && speed > 0) {
				return speed;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return abilities.getFlySpeed();
	}
	
}
