package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(EnderEyeItem.class)
public class EndEyeItemMixin {
	@Inject(method = "use",
			at = @At("HEAD")
			)
	private void teleportIfNeeded(World world, PlayerEntity player, Hand h, CallbackInfoReturnable<ActionResult> ci) {
		if(OptionManager.endEyeTeleport) {
			if(player instanceof ServerPlayerEntity && player.getAbilities().allowFlying) {
				ServerPlayerEntity sp = (ServerPlayerEntity)player;
				Vec3d start = sp.getPos().add(0, sp.getStandingEyeHeight(), 0);
				float r = 180;
				try {
					float f = OptionManager.maxEndEyeTpRadius;
					if(f >= 0) {
						r = f;
					}
				} catch (NumberFormatException e) {
				}
				
				HitResult hit = sp.getWorld().raycast(new RaycastContext(start, 
						start.add(sp.getRotationVector().multiply(r)), 
						RaycastContext.ShapeType.COLLIDER, 
						RaycastContext.FluidHandling.NONE, 
						sp));
				if(hit.getType() != HitResult.Type.MISS) {
					Vec3d pos = hit.getPos();
					sp.networkHandler.requestTeleport(pos.x, pos.y, pos.z, sp.getYaw(), sp.getPitch());
				}
			}
		}
	}
}
