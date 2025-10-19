package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ChunkTaskPrioritySystemInterface;
import lovexyn0827.mess.fakes.ChunkTicketManagerInterface;
import lovexyn0827.mess.fakes.ThreadedAnvilChunkStorageInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.registry.Registries;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageInterface {	
	@Shadow @Final
	private ServerWorld world;
	
	@Shadow @Final
	private ThreadedAnvilChunkStorage.TicketManager ticketManager;
	
	@Shadow @Final
	private ChunkTaskPrioritySystem chunkTaskPrioritySystem;
	
	@Shadow @Final
	private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
	
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
				StackTrace.blameCurrent(), Registries.CHUNK_STATUS.getId(requiredStatus));
	}
	
	@Inject(method = "method_43375", 
			at = @At(value = "HEAD"), 
			remap = false
	)
	private void onChunkLoad(ChunkPos pos, Optional<?> nbt, 
			CallbackInfoReturnable<Either<Chunk, ChunkHolder.Unloaded>> cir) {
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
				StackTrace.blameCurrent(), Registries.CHUNK_STATUS.getId(status));
	}
	
	@Inject(method = "method_43375", 
			at = @At(value = "RETURN"), 
			remap = false
	)
	private void onChunkLoadFinish(ChunkPos pos, Optional<?> nbt, 
			CallbackInfoReturnable<Either<Chunk, ChunkHolder.Unloaded>> cir) {
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
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				Registries.CHUNK_STATUS.getId(status));
	}
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/server/world/ThreadedAnvilChunkStorage."
							+ "ticketManager:Lnet/minecraft/server/world/ThreadedAnvilChunkStorage$TicketManager;", 
					opcode = Opcodes.PUTFIELD, 
					shift = At.Shift.AFTER
			)
	)
	private void onCreatedTicketManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, 
			StructureTemplateManager structureTemplateManager, Executor executor, 
			ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, 
			WorldGenerationProgressListener worldGenerationProgressListener, 
			ChunkStatusChangeListener chunkStatusChangeListener, 
			Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync, 
			CallbackInfo ci) {
		((ChunkTicketManagerInterface) this.ticketManager).initWorld(world);
	}
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/server/world/ThreadedAnvilChunkStorage."
							+ "chunkTaskPrioritySystem:Lnet/minecraft/server/world/ChunkTaskPrioritySystem;", 
					opcode = Opcodes.PUTFIELD, 
					shift = At.Shift.AFTER
			)
	)
	private void onCreatedChunkTaskPrioritySystem(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, 
			StructureTemplateManager structureTemplateManager, Executor executor, 
			ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, 
			WorldGenerationProgressListener worldGenerationProgressListener, 
			ChunkStatusChangeListener chunkStatusChangeListener, 
			Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync, 
			CallbackInfo ci) {
		// This is necessary since we couldn't ensure that advanced CTPSMixin is applied.
		if(this.chunkTaskPrioritySystem instanceof ChunkTaskPrioritySystemInterface) {
			((ChunkTaskPrioritySystemInterface) this.chunkTaskPrioritySystem).initWorld(world);
		}
	}
	
	@Override
	public final ChunkHolder getCHForMessMod(long pos) {
		return this.chunkHolders.get(pos);
	}
}
