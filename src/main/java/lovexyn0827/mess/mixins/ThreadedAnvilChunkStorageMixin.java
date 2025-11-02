package lovexyn0827.mess.mixins;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.DataFixer;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ChunkTicketManagerInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ThreadedAnvilChunkStorageMixin {	
	@Shadow @Final
	private ServerWorld world;
	
	@Shadow @Final
	private ServerChunkLoadingManager.TicketManager ticketManager;
	
	@Inject(method = "loadChunk", 
			at = @At(value = "HEAD")
	)
	private void onSchedulingChunkLoad(ChunkPos pos, 
			CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
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
	
//	@Inject(method = "convertToFullChunk", 
//			at = @At(value = "HEAD")
//	)
//	private void onSchedulingChunkGeneration(ChunkHolder chunkHolder, Chunk chunk, 
//			CallbackInfoReturnable<CompletableFuture<CompletableFuture<Chunk>>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_GENERATION, chunkHolder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
	
//	@Inject(method = "generate", 
//			at = @At(value = "HEAD")
//	)
//	private void onSchedulingChunkUdgrade(ChunkHolder holder, ChunkStatus requiredStatus, 
//			CallbackInfoReturnable<CompletableFuture<CompletableFuture<Chunk>>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.SCHEDULER_UPGRADE, holder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), 
//				StackTrace.blameCurrent(), Registries.CHUNK_STATUS.getId(requiredStatus));
//	}
//	
//	@Inject(method = "method_43375", 
//			at = @At(value = "HEAD"), 
//			remap = false
//	)
//	private void onChunkLoad(ChunkPos pos, Optional<?> nbt, 
//			CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.LOADING, pos.toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", 
//			at = @At(value = "HEAD")
//	)
//	private void onChunkUnload(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.UNLOADING, chunk.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "method_17227", 
//			at = @At(value = "HEAD"), 
//			remap = false
//	)
//	private void onChunkGeneration(ChunkHolder holder, Chunk chunk, 
//			CallbackInfoReturnable<CompletableFuture<CompletableFuture<Chunk>>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.GENERATION, holder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "method_17224", 
//			at = @At(value = "HEAD"), 
//			remap = false
//	)
//	private void onChunkUpgrade(ChunkPos pos, ChunkHolder holder, ChunkStatus status, Executor e, 
//			OptionalChunk<?> mayChunk, CallbackInfoReturnable<Boolean> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.UPGRADE, holder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), 
//				StackTrace.blameCurrent(), Registries.CHUNK_STATUS.getId(status));
//	}
//	
//	@Inject(method = "method_43375", 
//			at = @At(value = "RETURN"), 
//			remap = false
//	)
//	private void onChunkLoadFinish(ChunkPos pos, Optional<?> nbt, 
//			CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_LOADING, pos.toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", 
//			at = @At(value = "RETURN")
//	)
//	private void onChunkUnloadFinish(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_UNLOADING, chunk.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "method_17227", 
//			at = @At(value = "RETURN"), 
//			remap = false
//	)
//	private void onChunkGenerationFinish(ChunkHolder holder, Chunk chunk, 
//			CallbackInfoReturnable<CompletableFuture<CompletableFuture<Chunk>>> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_GENERATION, holder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), null);
//	}
//	
//	@Inject(method = "method_17224", 
//			at = @At(value = "RETURN"), 
//			remap = false
//	)
//	private void onChunkUpgradeFinish(ChunkPos pos, ChunkHolder holder, ChunkStatus status, Executor exec, 
//			OptionalChunk<?> mayChunk, CallbackInfoReturnable<Boolean> cir) {
//		if(ChunkBehaviorLogger.shouldSkip()) {
//			return;
//		}
//		
//		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.END_UPGRADE, holder.getPos().toLong(), 
//				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
//				Registries.CHUNK_STATUS.getId(status));
//	}
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "RETURN"
			), 
			require = 1
	)
	private void onCreatedTicketManager(
			ServerWorld world,
			LevelStorage.Session session,
			DataFixer dataFixer,
			StructureTemplateManager structureTemplateManager,
			Executor executor,
			ThreadExecutor<Runnable> mainThreadExecutor,
			ChunkProvider chunkProvider,
			ChunkGenerator chunkGenerator,
			WorldGenerationProgressListener worldGenerationProgressListener,
			ChunkStatusChangeListener chunkStatusChangeListener,
			Supplier<PersistentStateManager> persistentStateManagerFactory,
			int viewDistance,
			boolean dsync, 
			CallbackInfo ci) {
		((ChunkTicketManagerInterface) this.ticketManager).initWorld(world);
	}
}
