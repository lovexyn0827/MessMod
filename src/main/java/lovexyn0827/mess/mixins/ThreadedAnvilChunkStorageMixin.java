package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {
	@Shadow @Final
	private ServerWorld world;
	
	@Inject(method = "loadChunk", 
			at = @At(value = "HEAD")
	)
	private void onSchedulingChunkLoad(ChunkPos pos, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_LOADING, pos.toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}

	@Inject(method = "tryUnloadChunk", 
			at = @At(value = "HEAD")
	)
	private void onSchedulingChunkUnload(long pos, ChunkHolder chunkHolder, CallbackInfo ci) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_UNLOADING, pos, 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "convertToFullChunk", 
			at = @At(value = "HEAD")
	)
	private void onSchedulingChunkGeneration(ChunkHolder chunkHolder, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_GENERATION, chunkHolder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "upgradeChunk", 
			at = @At(value = "HEAD")
	)
	private void onSchedulingChunkUdgrade(ChunkHolder holder, ChunkStatus requiredStatus, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_UPGRADE, holder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), 
				StackTrace.blameCurrent(), requiredStatus.getId());
	}
	
	@Inject(method = "method_17256", 
			at = @At(value = "HEAD"), 
			remap = false
	)
	private void onChunkLoad(ChunkPos pos, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.LOADING, pos.toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", 
			at = @At(value = "HEAD")
	)
	private void onChunkUnload(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.UNLOADING, chunk.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "method_20460", 
			at = @At(value = "HEAD"), 
			remap = false
	)
	private void onChunkGeneration(ChunkHolder holder, Either<?, ?> either, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.GENERATION, holder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "method_17225", 
			at = @At(value = "HEAD"), 
			remap = false
	)
	private void onChunkUpgrade(ChunkPos pos, ChunkHolder holder, ChunkStatus status, Executor e, 
			List<?> list, CallbackInfoReturnable<Boolean> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.UPGRADE, holder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), 
				StackTrace.blameCurrent(), status.getId());
	}
	
	@Inject(method = "method_17256", 
			at = @At(value = "RETURN"), 
			remap = false
	)
	private void onChunkLoadFinish(ChunkPos pos, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_LOADING, pos.toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", 
			at = @At(value = "RETURN")
	)
	private void onChunkUnloadFinish(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_UNLOADING, chunk.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "method_20460", 
			at = @At(value = "RETURN"), 
			remap = false
	)
	private void onChunkGenerationFinish(ChunkHolder holder, Either<?, ?> either, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_GENERATION, holder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
	}
	
	@Inject(method = "method_17225", 
			at = @At(value = "RETURN"), 
			remap = false
	)
	private void onChunkUpgradeFinish(ChunkPos pos, ChunkHolder holder, ChunkStatus status, Executor exec, 
			List<?> list, CallbackInfoReturnable<Boolean> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_UPGRADE, holder.getPos().toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), status.getId());
	}
}
