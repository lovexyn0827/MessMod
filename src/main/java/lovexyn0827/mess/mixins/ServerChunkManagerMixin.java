package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Shadow
	private ServerWorld world;
	
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
