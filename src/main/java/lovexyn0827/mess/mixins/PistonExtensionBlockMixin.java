package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

@Mixin(PistonExtensionBlock.class)
public class PistonExtensionBlockMixin {
	@Shadow
	private VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return null;
	}
	
	@Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
	private void replaceOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, 
			CallbackInfoReturnable<VoxelShape> cir) {
		if(OptionManager.interactableB36) {
			cir.setReturnValue(this.getCollisionShape(state, world, pos, context));
		}
	}
}
