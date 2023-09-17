package lovexyn0827.mess.mixins;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;

@Mixin(ServerChunkManager.MainThreadExecutor.class)
public abstract class ServerChunkManagerMainThreadExecutorMixin extends ThreadExecutor<Runnable> {
	@Shadow(remap = false)
	private @Final ServerChunkManager field_18810;
	
	private static final AtomicLong NEXT_ID = new AtomicLong(0);
	private static final Map<Object, Long> TASK_TO_ID = new ConcurrentHashMap<>();
	
	protected ServerChunkManagerMainThreadExecutorMixin(String name) {
		super(name);
	}
	
	@Inject(method = "executeTask", at = @At("HEAD"))
	protected void onExecuteTask(Runnable task, CallbackInfo ci) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.ASYNC_TASK_SINGLE, ChunkPos.MARKER, 
				this.field_18810.getWorld().getRegistryKey().getValue(), Thread.currentThread(), StackTrace.blameCurrent(), 
				TASK_TO_ID.remove(task));
	}
	
	@Override
	public void runTasks(BooleanSupplier stopCondition) {
		if(!ChunkBehaviorLogger.shouldSkip()) {
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.ASYNC_TASKS, ChunkPos.MARKER, 
					this.field_18810.getWorld().getRegistryKey().getValue(), Thread.currentThread(), 
					StackTrace.blameCurrent(), this.getTaskCount());
		}
		
		super.runTasks(stopCondition);
	}
	
	@Override
	public void send(Runnable runnable) {
		if(!ChunkBehaviorLogger.shouldSkip()) {
			long id = NEXT_ID.getAndIncrement();
			TASK_TO_ID.put(runnable, id);
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.ASYNC_TASK_ADDITION, ChunkPos.MARKER, 
					this.field_18810.getWorld().getRegistryKey().getValue(), Thread.currentThread(), 
					StackTrace.blameCurrent(), id);
		}
		
		super.send(runnable);
	}
}
