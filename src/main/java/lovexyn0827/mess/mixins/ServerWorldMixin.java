package lovexyn0827.mess.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.TickingPhase;
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

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements BlockView {
	@SuppressWarnings("deprecation")
	@Override
	public BlockHitResult raycast(RaycastContext context2) {
		return BlockView.raycast(context2.getStart(), context2.getEnd(), context2, (context, pos) -> {
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
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=chunkSource")
			)
	private void startChunkTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.WEATHER_CYCLE, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=tickPending")
			)
	private void startScheduledTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.CHUNK, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=raid")
			)
	private void startVillageTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.SCHEDULED_TICK, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", 
					args = "ldc=blockEvents")
			)
	private void startBlockEventTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.VILLAGE, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE_STRING", 
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", 
					args = "ldc=entities")
			)
	private void startEntityTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.BLOCK_EVENT, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V")
			)
	private void startBlockEntityTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.ENTITY, (ServerWorld)(Object) this);
	}
	
	@Inject(method = "tick", 
			at = @At("RETURN")
			)
	private void endTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		MessMod.INSTANCE.getServerHudManager().sidebar.updateData(TickingPhase.TILE_ENTITY, (ServerWorld)(Object) this);
	}
}
