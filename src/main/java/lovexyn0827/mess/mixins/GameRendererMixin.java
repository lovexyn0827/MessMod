package lovexyn0827.mess.mixins;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyArg(method = "findCrosshairTarget", 
			at = @At(
					value = "INVOKE", 
					target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast"
							+ "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;"
							+ "Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), 
			remap = false
	)
	private static Predicate<?> allowNonCollidableEntities(Predicate<?> p) {
		if(OptionManager.allowTargetingSpecialEntities) {
			return (e) ->true;
		} else {
			return p;
		}
	}
}
