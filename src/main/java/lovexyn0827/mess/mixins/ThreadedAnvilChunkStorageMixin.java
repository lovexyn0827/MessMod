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
import lovexyn0827.mess.fakes.ThreadedAnvilChunkStorageInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
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
public abstract class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageInterface {	
	@Shadow @Final
	private ServerWorld world;
	
	@Shadow @Final
	private ServerChunkLoadingManager.TicketManager ticketManager;
	@Shadow @Final
	private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
	

	
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
	
	@Override
	public final ChunkHolder getCHForMessMod(long pos) {
		return this.chunkHolders.get(pos);
	}
}
