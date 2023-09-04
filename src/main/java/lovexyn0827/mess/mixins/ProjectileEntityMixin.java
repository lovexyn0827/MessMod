package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {
	private static final ChunkTicketType<? super Entity> ENTITY_TICKET = ChunkTicketType.create("projectile", (a, b) -> 1, 3);
	private static final ChunkTicketType<? super Entity> PERMANENT_ENTITY_TICKET = ChunkTicketType.create("projectile_permanent", (a, b) -> 1);
	private ProjectileEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@SuppressWarnings("resource")
	@Inject(method = "tick",
			at = @At("TAIL")
			)
	private void loadChunkIfNeeded(CallbackInfo ci) {
		if(!this.getWorld().isClient) {
			// Firework rockets are not supported because their movements are hard to predict.
			if(OptionManager.projectileChunkLoading && !((Object)this instanceof FireworkRocketEntity)) {
				ServerWorld world = (ServerWorld)this.getWorld();
				Vec3d nextPos = this.getPos().add(this.getVelocity());
				ChunkTicketType<? super Entity> tt = OptionManager.projectileChunkLoadingPermanence ? PERMANENT_ENTITY_TICKET : ENTITY_TICKET;
				world.getServer().submitAndJoin(() -> 
					world.getChunkManager().addTicket(tt, new ChunkPos((int)(nextPos.x / 16), 
							(int)(nextPos.z / 16)), OptionManager.projectileChunkLoadingRange, this));
			}
		}
	}
	
	@ModifyVariable(method = "setVelocity", 
			at = @At("HEAD"), 
			ordinal = 1)
	private float tryRemoveRandomness(float dIn) {
		return OptionManager.disableProjectileRandomness ? 0.0F : dIn * OptionManager.projectileRandomnessScale;
	}
}
