package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import lovexyn0827.mess.options.OptionManager;
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
	@Override
	public BlockHitResult raycast(RaycastContext context) {
		return (BlockHitResult)BlockView.raycast(context, (raycastContext, blockPos) -> {
			if(OptionManager.skipUnloadedChunkInRaycasting) {
				if(!((ServerWorld)(Object) this).isChunkLoaded(blockPos)) {
					return null;
				}
			}
			
			BlockState blockState = this.getBlockState(blockPos);
			FluidState fluidState = this.getFluidState(blockPos);
			Vec3d vec3d = raycastContext.getStart();
			Vec3d vec3d2 = raycastContext.getEnd();
			VoxelShape voxelShape = raycastContext.getBlockShape(blockState, this, blockPos);
			BlockHitResult blockHitResult = this.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
			VoxelShape voxelShape2 = raycastContext.getFluidShape(fluidState, this, blockPos);
			BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
			double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
			double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
			return d <= e ? blockHitResult : blockHitResult2;
		}, (raycastContext) -> {
			Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
			return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(raycastContext.getEnd()));
		});
	}
}
