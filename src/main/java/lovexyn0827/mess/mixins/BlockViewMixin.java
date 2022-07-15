package lovexyn0827.mess.mixins;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.WorldView;

@Mixin(BlockView.class)
public interface BlockViewMixin {
	//@SuppressWarnings("target")
//	@Inject(method = "method_17743",
//			require = 0, 
//			at = @At("HEAD"),
//			cancellable = true
//			)
//	default void skipUnloaded(RaycastContext rc, BlockPos pos, CallbackInfoReturnable<BlockHitResult> cir) {
//		if(OptionManager.skipUnloadedChunkInRaycasting) {
//			if(!(this instanceof WorldView) && ((WorldView) this).isChunkLoaded(pos)) {
//				cir.setReturnValue(null);
//				cir.cancel();
//			}
//		}
//	}
	//T raycast(RaycastContext raycastContext, BiFunction<RaycastContext, BlockPos, T> context, 
	//		Function<RaycastContext, T> blockRaycaster)
//	@Redirect(method = "raycast(Lnet/minecraft/world/RaycastContext;Ljava/util/function/BiFunction;Ljava/util/function/Function;)",
//			at = @At(value = "INVOKE", 
//					target = "Ljava/util/function/BiFunction;apply(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
//			)
//	static void skipUnloaded(RaycastContext rc, BlockPos pos, CallbackInfoReturnable<BlockHitResult> cir) {
//		if(OptionManager.skipUnloadedChunkInRaycasting) {
//			if(!(this instanceof WorldView) && ((WorldView) this).isChunkLoaded(pos)) {
//				cir.setReturnValue(null);
//				cir.cancel();
//			}
//		}
//	}
}
