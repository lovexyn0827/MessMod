package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
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
							+ "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
			index = 1)
	private BlockState replaceToCustomBlockState(BlockState blockState) {
		BlockState customBlockState = SetExplosionBlockCommand.getBlockState();
		return customBlockState == null ? blockState : customBlockState;
	}
	
	@ModifyArg(method = "affectWorld",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;setBlockState"
							+ "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"),
			index = 1)
	private BlockState replaceToCustomFireState(BlockState fireState) {
		BlockState customFireState = SetExplosionBlockCommand.getFireState();
		return customFireState == null ? fireState : customFireState;
	}
	
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
			double d, double e, double f, double g, double h, int i, int j, float k, float l, float m, double n, double o, double p, 
			Vec3d vec3d
			) {
		if(entity.world instanceof ServerWorld) {
			ServerWorld world = (ServerWorld)(entity.world);
			if(OptionManager.entityExplosionRaysVisiblity) {
				HitResult hit = world.raycast(new RaycastContext(source, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
				ShapeSender sr = MessMod.INSTANCE.shapeSender;
				sr.addShape(new RenderedLine(source, hit.getPos(), 0x00FF00FF, 
						OptionManager.entityExplosionRaysLifetime, world.getTime()), entity.getEntityWorld().getRegistryKey());
				sr.addShape(new RenderedLine(hit.getPos(), vec3d, 0xFF0000FF, 
						OptionManager.entityExplosionRaysLifetime, world.getTime()), entity.getEntityWorld().getRegistryKey());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Inject(method = "collectBlocksAndDamageEntities", 
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onExplosionInfluencedEntity(CallbackInfo ci, Set set, int i, float j, int k, int l, int d, int q, 
			int e, int r, List f, Vec3d vec3d, int g, Entity entity, double h, double s, double t, double u, 
			double blockPos, double fluidState, double v) {
		// FIXME: Not compatible with Lithium, may be fixed via disabling the Mixin of it.
		if(OptionManager.entityExplosionInfluence && !entity.world.isClient) {
			StringBuilder entityInfoBuilder = new StringBuilder(entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "")).
					append("[").append(entity.getId()).append(",").append(entity.getPos()).append("]");
			MessMod.INSTANCE.sendMessageToEveryone("Affected Entity: ", entityInfoBuilder.toString(), "\n", 
					"Exposure: ", fluidState, "\n", 
					"Infulence: ", v);
			
		}
	}
	
	@Redirect(method = "collectBlocksAndDamageEntities", 
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/explosion/Explosion;getExposure"
							+ "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"
			)
	)
	private float tryDisableExpsure(Vec3d pos, Entity e) {
		return OptionManager.disableExplosionExposureCalculation ? 1.0F : Explosion.getExposure(pos, e);
	}
}
