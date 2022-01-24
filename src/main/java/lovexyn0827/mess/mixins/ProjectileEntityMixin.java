package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
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
	private ProjectileEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick",
			at = @At("TAIL")
			)
	public void loadChunkIfNeeded(CallbackInfo ci) {
		if(!this.world.isClient) {
			// Firework rockets are not supported because their movements are hard to predict.
			if(MessMod.INSTANCE.getBooleanOption("projectileChunkLoading") && !((Object)this instanceof FireworkRocketEntity)) {
				ServerWorld world = (ServerWorld)this.world;
				Vec3d nextPos = this.getPos().add(this.getVelocity());
				world.getServer().submitAndJoin(() -> 
					world.getChunkManager().addTicket(ENTITY_TICKET,new ChunkPos((int)(nextPos.x / 16), (int)(nextPos.z / 16)), 3, this));
			}
		}
	}
	
	@ModifyVariable(method = "setVelocity", 
			at = @At("HEAD"), 
			ordinal = 1)
	public float tryRemoveRandomness(float dIn) {
		return MessMod.INSTANCE.getBooleanOption("disableProjectileRandomness") ? 0.0F : dIn;
	}
}
