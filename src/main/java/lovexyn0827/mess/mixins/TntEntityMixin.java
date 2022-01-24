package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
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
	private static final ChunkTicketType<? super Entity> ENTITY_TICKET = ChunkTicketType.create("tnt*.j", (a, b) -> 1, 3);
	private TntEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick",
			at = @At("TAIL")
			)
	public void loadChunkIfNeeded(CallbackInfo ci) {
		if(!this.world.isClient) {
			if(MessMod.INSTANCE.getBooleanOption("tntChunkLoading")) {
				ServerWorld world = (ServerWorld)this.world;
				Vec3d nextPos = this.getPos();
				world.getServer().submitAndJoin(() -> world.getChunkManager().addTicket(ENTITY_TICKET,
						new ChunkPos((int)(nextPos.x / 16), (int)(nextPos.z / 16)), 3, this));
			}
		}
	}
}
