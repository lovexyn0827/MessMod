package lovexyn0827.mess.mixins;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.datafixers.DataFixer;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import lovexyn0827.mess.fakes.ChunkTaskPrioritySystemInterface;
import lovexyn0827.mess.fakes.ChunkTicketManagerInterface;
import lovexyn0827.mess.fakes.ThreadedAnvilChunkStorageInterface;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage.Session;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageInterface {	
	@Shadow @Final
	private ServerWorld world;
	
	@Shadow @Final
	private ServerChunkLoadingManager.TicketManager ticketManager;
	
	@Shadow @Final
	private ChunkTaskPrioritySystem chunkTaskPrioritySystem;
	
	@Shadow @Final
	private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/server/world/ServerChunkLoadingManager."
							+ "ticketManager:Lnet/minecraft/server/world/ServerChunkLoadingManager$TicketManager;", 
					opcode = Opcodes.PUTFIELD, 
					shift = At.Shift.AFTER
			)
	)
	private void onCreatedTicketManager(
			ServerWorld world,
			Session session,
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
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/server/world/ServerChunkLoadingManager."
							+ "chunkTaskPrioritySystem:Lnet/minecraft/server/world/ChunkTaskPrioritySystem;", 
					opcode = Opcodes.PUTFIELD, 
					shift = At.Shift.AFTER
			)
	)
	private void onCreatedChunkTaskPrioritySystem(
			ServerWorld world,
			Session session,
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
