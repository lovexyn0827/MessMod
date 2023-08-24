package lovexyn0827.mess.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.PulseRecorder;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements BlockView, ServerWorldInterface {
	private final PulseRecorder pulseRecorder = new PulseRecorder();
	
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
            return BlockHitResult.createMissed(context2.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context2.getEnd()));
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
		ServerTickingPhase.REST.begin((ServerWorld)(Object) this);
	}
	
	@Override
	public PulseRecorder getPulseRecorder() {
		return this.pulseRecorder;
	}
}
