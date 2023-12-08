package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.LazyLoadCommand;
import lovexyn0827.mess.fakes.EntityInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Shadow
	private ServerWorld world;
	
	@Inject(method = "shouldTickEntity", at = @At("HEAD"), cancellable = true)
	private void tickEntityIfNeeded(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if(((EntityInterface) entity).isFrozen()) {
			cir.setReturnValue(false);
			cir.cancel();
		} else if(!LazyLoadCommand.LAZY_CHUNKS.isEmpty()) {
			long pos = ChunkPos.toLong(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4);
			if(LazyLoadCommand.LAZY_CHUNKS.containsKey(this.world.getRegistryKey())
					&& LazyLoadCommand.LAZY_CHUNKS.get(this.world.getRegistryKey()).contains(pos)) {
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}
	
	@Inject(method = "initChunkCaches", at = @At("HEAD"))
	protected void onInitCaches(CallbackInfo cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCM_INIT_CACHE, ChunkPos.MARKER, 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				null);
	}
	
	@Inject(method = "tick()Z", at = @At("JUMP"), locals = LocalCapture.CAPTURE_FAILHARD)
	protected void onTickNoArg(CallbackInfoReturnable<Boolean> cir, boolean blCTM, boolean blTACS) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCM_TICK, ChunkPos.MARKER, 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				String.format("CTM: %s; TACS: %s", blCTM, blTACS));
	}
}
