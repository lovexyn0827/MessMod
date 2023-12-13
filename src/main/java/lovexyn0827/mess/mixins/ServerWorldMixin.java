package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.EntityInterface;
import lovexyn0827.mess.fakes.ServerEntityManagerInterface;
import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.PulseRecorder;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements BlockView, ServerWorldInterface {
	private final PulseRecorder pulseRecorder = new PulseRecorder();
	
	@Shadow
	private @Final ServerEntityManager<?> entityManager;
	
	@Override
	public BlockHitResult raycast(RaycastContext context2) {
		return BlockView.raycast(context2.getStart(), context2.getEnd(), context2, (context, pos) -> {
		// TODO Use better approach or copy newer code
			if(OptionManager.skipUnloadedChunkInRaycasting) {
				if(!((ServerWorld)(Object) this).isChunkLoaded(pos)) {
					return null;
				}
			}
			
            BlockState blockState = this.getBlockState((BlockPos)pos);
            FluidState fluidState = this.getFluidState((BlockPos)pos);
            Vec3d vec3d = context.getStart();
            Vec3d vec3d2 = context.getEnd();
            VoxelShape voxelShape = context.getBlockShape(blockState, this, (BlockPos)pos);
            BlockHitResult blockHitResult = this.raycastBlock(vec3d, vec3d2, (BlockPos)pos, voxelShape, blockState);
            VoxelShape voxelShape2 = context.getFluidShape(fluidState, this, (BlockPos)pos);
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, (BlockPos)pos);
            double d = blockHitResult == null ? Double.MAX_VALUE : context.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : context.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, context -> {
            Vec3d vec3d = context.getStart().subtract(context.getEnd());
            return BlockHitResult.createMissed(context.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context.getEnd()));
        });
	}
	
	@Inject(method = "tick", 
			at = @At("HEAD")
			)
	private void startTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickingPhase.WEATHER_CYCLE.begin((ServerWorld)(Object) this);
		// Actually here is also the ending of WTU
		if(((ServerWorld)(Object) this).getRegistryKey() == World.OVERWORLD) {
			MessMod.INSTANCE.updateTime(((ServerWorld)(Object) this).getTime());
		}
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
	
	@Inject(method = "method_31420", at = @At("HEAD"), cancellable = true, remap = false)
	private void skipFrozenEntity(Profiler profiler, Entity entity, CallbackInfo ci) {
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
	private void onCreatedEntityManager(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, 
			ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, 
			WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, 
			boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, CallbackInfo ci) {
		((ServerEntityManagerInterface) this.entityManager).initWorld((ServerWorld)(Object) this);
	}
}
