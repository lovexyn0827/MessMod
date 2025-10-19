package lovexyn0827.mess.mixins;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.SetExplosionBlockCommand;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.RenderedLine;
import lovexyn0827.mess.rendering.ShapeSender;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
	@ModifyArg(method = "affectWorld",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState"
							+ "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"),
			index = 1)
	private BlockState replaceToCustomFireState(BlockState fireState) {
		BlockState customFireState = SetExplosionBlockCommand.getFireState();
		return customFireState == null ? fireState : customFireState;
	}
	
	// Actually this version is better as invocations is fewer.
	@Inject(method = "getExposure",
			at = @At(value = "INVOKE",
							target = "Lnet/minecraft/world/World;raycast"
									+ "(Lnet/minecraft/world/RaycastContext;)Lnet/minecraft/util/hit/BlockHitResult;", 
							args = "log"
					),
			locals = LocalCapture.CAPTURE_FAILHARD
			)
	private static void renderLines(Vec3d source, 
			Entity entity, 
			CallbackInfoReturnable<Float> cir, 
			Box box, 
			double d, double e, double f, double g, double h, int i, int j, double k, double l, double m, double n, double o, double p, 
			Vec3d vec3d
			) {
		if(entity.getWorld() instanceof ServerWorld) {
			ServerWorld world = (ServerWorld)(entity.getWorld());
			if(OptionManager.entityExplosionRaysVisiblity) {
				HitResult hit = world.raycast(new RaycastContext(source, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
				ShapeSender sr = MessMod.INSTANCE.shapeSender;
				sr.addShape(new RenderedLine(source, hit.getPos(), 0x00FF00FF, 
						OptionManager.entityExplosionRaysLifetime, world.getTime()), 
						entity.getEntityWorld().getRegistryKey(), 
						null);
				sr.addShape(new RenderedLine(hit.getPos(), vec3d, 0xFF0000FF, 
						OptionManager.entityExplosionRaysLifetime, world.getTime()), 
						entity.getEntityWorld().getRegistryKey(), 
						null);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Inject(method = "collectBlocksAndDamageEntities", 
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
			),
			locals = LocalCapture.CAPTURE_FAILSOFT, 
			require = 0
	)
	private void onExplosionInfluencedEntity(CallbackInfo ci, Set set, int i, float q, 
			int k, int l, int r, int s, int t, int u, List list, Vec3d vec3d, 
			Iterator var12, Entity entity, double v, double w, double x, double y, 
			double z, double aa, double ab, Vec3d injectorAllocatedLocal32, 
			Entity injectorAllocatedLocal33) {
		// FIXME: Not compatible with Lithium, may be fixed via disabling the Mixin of it.
		if(OptionManager.entityExplosionInfluence && !entity.getWorld().isClient) {
			StringBuilder entityInfoBuilder = new StringBuilder(entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "")).
					append("[").append(entity.getId()).append(",").append(entity.getPos()).append("]");
			MessMod.INSTANCE.sendMessageToEveryone("Affected Entity: ", entityInfoBuilder.toString(), "\n", 
					"Exposure: ", aa, "\n", 
					"Infulence: ", v);
			
		}
	}
	
	@Inject(method = "getExposure", at = @At("HEAD"), cancellable = true)
	private static void tryDisableExpsure(Vec3d pos, Entity e, CallbackInfoReturnable<Float> cir) {
		if(OptionManager.disableExplosionExposureCalculation) {
			cir.setReturnValue(1.0F);
			cir.cancel();
		}
	}
	
	@ModifyVariable(method = "collectBlocksAndDamageEntities", 
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", 
					shift = At.Shift.BY, 
					by = -2
			), 
			name = "vec3d2"
	)
	private Vec3d scaleImpulse(Vec3d in) {
		return in.multiply(OptionManager.entityExplosionImpulseScale);
	}
}
