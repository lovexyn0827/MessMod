package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.command.LazyLoadCommand;
import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Inject(method = "shouldTickEntity", at = @At("HEAD"), cancellable = true)
	private void tickEntityIfNeeded(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if(((EntityInterface) entity).isFrozen()) {
			cir.setReturnValue(false);
			cir.cancel();
		} else if(!LazyLoadCommand.LAZY_CHUNKS.isEmpty()) {
			long pos = ChunkPos.toLong(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4);
			if(LazyLoadCommand.LAZY_CHUNKS.contains(pos)) {
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}
}
