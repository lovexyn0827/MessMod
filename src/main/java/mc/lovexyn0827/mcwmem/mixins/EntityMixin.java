package mc.lovexyn0827.mcwmem.mixins;

import java.util.stream.Stream;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import mc.lovexyn0827.mcwmem.command.EntityConfigCommand;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.ReusableStream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@SuppressWarnings("rawtypes")
	@Inject(at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/entity/Entity;stepHeight:F",
					opcode = Opcodes.GETFIELD
			), 
			cancellable = true,
			method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void onCalculatingStepHeight(Vec3d movement, 
			CallbackInfoReturnable<Vec3d> ci, 
			Box box, 
			ShapeContext shapeContext, 
			Stream stream, Stream stream2, 
			ReusableStream reusableStream, 
			Vec3d vec3d) {
		Entity entity = (Entity)(Object)this;
		if(EntityConfigCommand.shouldDisableStepHeight(entity)) {
			ci.setReturnValue(vec3d);
			ci.cancel();
		}
	}
}
