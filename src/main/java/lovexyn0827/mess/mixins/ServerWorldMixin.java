package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.fakes.EntityInterface;
import lovexyn0827.mess.fakes.ServerEntityManagerInterface;
import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.NoChunkLoadingWorld;
import lovexyn0827.mess.util.PulseRecorder;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.Spawner;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickManager;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements BlockView, ServerWorldInterface {
	private final PulseRecorder pulseRecorder = new PulseRecorder();
	private @Final NoChunkLoadingWorld noChunkLoadingWorld;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onCreated(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState, CallbackInfo ci) {
		this.noChunkLoadingWorld = new NoChunkLoadingWorld((ServerWorld) (Object) this);
	}
	
	@Shadow
	private @Final ServerEntityManager<?> entityManager;
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockHitResult raycast(RaycastContext context) {
		// TODO Use better approach or copy newer code
		return (BlockHitResult)BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
			if(OptionManager.skipUnloadedChunkInRaycasting) {
				if(!((ServerWorld)(Object) this).isChunkLoaded(blockPos)) {
					return null;
				}
			}
			
            BlockState blockState = this.getBlockState((BlockPos)blockPos);
            FluidState fluidState = this.getFluidState((BlockPos)blockPos);
            Vec3d vec3d = context.getStart();
            Vec3d vec3d2 = context.getEnd();
            VoxelShape voxelShape = context.getBlockShape(blockState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult = this.raycastBlock(vec3d, vec3d2, (BlockPos)blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = context.getFluidShape(fluidState, this, (BlockPos)blockPos);
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, (BlockPos)blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : context.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : context.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, context2 -> {
            Vec3d vec3d = context2.getStart().subtract(context2.getEnd());
            return BlockHitResult.createMissed(context2.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(context2.getEnd()));
        });
	}
	
	@Inject(method = "tick", 
			at = @At("HEAD")
			)
	private void startTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.WEATHER_CYCLE.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=chunkSource")
			)
	private void startChunkTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.CHUNK.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=tickPending")
			)
	private void startScheduledTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.SCHEDULED_TICK.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=raid")
			)
	private void startVillageTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.VILLAGE.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=blockEvents")
			)
	private void startBlockEventTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.BLOCK_EVENT.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", 
					args = "ldc=entities")
			)
	private void startEntityTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.ENTITY.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V")
			)
	private void startBlockEntityTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.TILE_ENTITY.begin((ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At("RETURN")
			)
	private void endTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.DIM_REST.begin((ServerWorld)(Object) this);
	}
	
	@Override
	public PulseRecorder getPulseRecorder() {
		return this.pulseRecorder;
	}
	
	@Inject(
			method = "method_31420", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/server/world/ChunkTicketManager.shouldTickEntities(J)Z"
			), 
			cancellable = true, 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void skipTickingEntityIfNeeded(TickManager tickManager, Profiler profiler, 
			Entity entity, CallbackInfo ci) {
		if(((EntityInterface) entity).isFrozen()) {
			ci.cancel();
		}
	}
	
	@Inject(
			method = "<init>", 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/server/world/ServerWorld.entityManager:Lnet/minecraft/server/world/ServerEntityManager;", 
					opcode = Opcodes.PUTFIELD, 
					shift = At.Shift.AFTER
			)
	)
	private void onCreatedEntityManager(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState, CallbackInfo ci) {
		((ServerEntityManagerInterface) this.entityManager).initWorld((ServerWorld)(Object) this);
	}
	
	public NoChunkLoadingWorld toNoChunkLoadingWorld() {
		return this.noChunkLoadingWorld;
	}
}
