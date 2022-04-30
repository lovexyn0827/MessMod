package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity{
	private static final ChunkTicketType<? super Entity> ENTITY_TICKET = ChunkTicketType.create("tnt", (a, b) -> 1, 3);
	private static final ChunkTicketType<? super Entity> PERMANENT_ENTITY_TICKET = ChunkTicketType.create("tnt_permanent", (a, b) -> 1);
	
	private TntEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick",
			at = @At("TAIL")
			)
	private void loadChunkIfNeeded(CallbackInfo ci) {
		if(!this.world.isClient) {
			if(OptionManager.tntChunkLoading) {
				ServerWorld world = (ServerWorld)this.world;
				Vec3d nextPos = this.getPos();
				ChunkTicketType<? super Entity> tt = OptionManager.tntChunkLoadingPermanence ? PERMANENT_ENTITY_TICKET : ENTITY_TICKET;
				world.getServer().submitAndJoin(() -> world.getChunkManager().addTicket(tt,
						new ChunkPos((int)(nextPos.x / 16), (int)(nextPos.z / 16)), OptionManager.tntChunkLoadingRange, this));
			}
		}
	}
	
	@Override
	public boolean handleAttack(Entity attacker) {
		if(OptionManager.attackableTnt) {
			this.remove();
			if(attacker.isSneaking()) {
				this.world.getEntitiesByType(EntityType.TNT, this.getBoundingBox(), (e) -> true).forEach(Entity::remove);
			}
			
			return true;
		} else {
			return super.handleAttack(attacker);
		}
	}
}
