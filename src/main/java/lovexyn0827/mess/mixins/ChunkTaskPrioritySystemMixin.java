package lovexyn0827.mess.mixins;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ChunkTaskPrioritySystemInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.log.chunk.ChunkTaskPrintUtil;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;

@Mixin(ChunkTaskPrioritySystem.class)
public class ChunkTaskPrioritySystemMixin implements ChunkTaskPrioritySystemInterface {
	@Unique
	private static final AtomicLong NEXT_ID = new AtomicLong(0L);
	
	// We still use ID maps here as auto-increasing IDs are unlikely to collide
	@Unique
	private static final Map<Object, Long> IDS_BY_TASK = Collections.synchronizedMap(new WeakHashMap<>());
	
	@Unique
	private ServerWorld world;

	@Override
	public void initWorld(ServerWorld world) {
		IDS_BY_TASK.clear();
		NEXT_ID.set(0);
		this.world = world;
	}

	@Inject(method = "updateLevel", at = @At("HEAD"))
	private void onLevelUpdate(ChunkPos pos, IntSupplier levelGetter, int targetLevel, IntConsumer levelSetter, 
			CallbackInfo ci) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.CTPS_LEVEL, pos.toLong(), 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				targetLevel);
	}
	
	@Inject(method = "removeChunk", at = @At("HEAD"))
	private void onRemoveChunk(MessageListener<?> actor, long chunkPos, Runnable callback, boolean clearTask, 
			CallbackInfo ci) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		long taskId = IDS_BY_TASK.computeIfAbsent(callback, (k) -> NEXT_ID.getAndIncrement());
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.CTPS_REMOVE, chunkPos, 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				ChunkTaskPrintUtil.printTask(taskId, callback) + ':' + clearTask + '@' + actor.getName());
	}
	
	@Inject(method = "enqueueChunk", at = @At("HEAD"))
	private void onEnqueueChunk(MessageListener<?> actor, Function<?, ?> task, long chunkPos, 
			IntSupplier lastLevelUpdatedToProvider, boolean addBlocker, 
			CallbackInfo ci) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		long taskId = IDS_BY_TASK.computeIfAbsent(task, (k) -> NEXT_ID.getAndIncrement());
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.CTPS_CHUNK, chunkPos, 
				this.world.getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				ChunkTaskPrintUtil.printTask(taskId, task) + ':' + addBlocker + '@' + actor.getName());
	}
}
