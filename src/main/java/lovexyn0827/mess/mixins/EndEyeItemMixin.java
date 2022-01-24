package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.MessMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(EnderEyeItem.class)
public class EndEyeItemMixin {
	@Inject(method = "use",
			at = @At("HEAD")
			)
	public void loadChunkIfNeeded(World world, PlayerEntity player, Hand h, CallbackInfoReturnable<TypedActionResult<?>> ci) {
		if(MessMod.INSTANCE.getBooleanOption("endEyeTeleport")) {
			if(player instanceof ServerPlayerEntity && player.abilities.allowFlying) {
				ServerPlayerEntity sp = (ServerPlayerEntity)player;
				Vec3d start = sp.getPos().add(0, sp.getStandingEyeHeight(), 0);
				float r = 180;
				try {
					float f = Float.parseFloat(MessMod.INSTANCE.getOption("maxEndEyeTpRadius"));
					if(f >= 0) {
						r = f;
					}
				} catch (NumberFormatException e) {
				}
				
				HitResult hit = sp.world.raycast(new RaycastContext(start, 
						start.add(sp.getRotationVector().multiply(r)), 
						RaycastContext.ShapeType.COLLIDER, 
						RaycastContext.FluidHandling.NONE, 
						sp));
				if(hit.getType() != HitResult.Type.MISS) {
					Vec3d pos = hit.getPos();
					sp.getServerWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, 
							new ChunkPos(((int)sp.chunkX), ((int)sp.chunkZ)), 1, sp.getEntityId());
					sp.networkHandler.requestTeleport(pos.x, pos.y, pos.z, sp.yaw, sp.pitch);
				}
			}
		}
	}
}
