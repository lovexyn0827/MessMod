package lovexyn0827.mess.mixins;

import java.util.List;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Lists;

import lovexyn0827.mess.command.EntityConfigCommand;
import lovexyn0827.mess.command.LogMovementCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow private Vec3d pos;
	@Shadow private Vec3d velocity;
	@Shadow private int id;
	@Shadow private World world;
	@Shadow private EntityType<?> type;
	private static List<Text> currentReport;
	private static Vec3d lastMovement;

	@Shadow protected abstract Vec3d adjustMovementForPiston(Vec3d movement);
	@Shadow protected abstract Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type);
	@Shadow protected abstract Vec3d adjustMovementForCollisions(Vec3d movement);
	
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
	private void onCalculatingStepHeight(Vec3d movement, CallbackInfoReturnable<Vec3d> cir, 
			Box box, List list, Vec3d vec3d) {
		Entity entity = (Entity)(Object)this;
		if(EntityConfigCommand.shouldDisableStepHeight(entity)) {
			cir.setReturnValue(vec3d);
			cir.cancel();
		}
	}
	
	@Inject(method = "move", 
			at = @At("HEAD")
	)
	private void onMoveStart(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(type != MovementType.SELF && type != MovementType.PLAYER) {
			if(LogMovementCommand.SUBSCRIBED_ENTITIES.contains((Entity)(Object) this) && !this.world.isClient) {
				currentReport = Lists.newArrayList();
				currentReport.add(new LiteralText("Tick: " + this.world.getTime()).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
				currentReport.add(new LiteralText("Entity: " + this.type + '(' + this.id + ')'));
				String typeStr = "Unknown";
				switch(type) {
				case PISTON :
					typeStr = "Piston";
					break;
				case SHULKER :
					typeStr = "Shulker";
					break;
				case SHULKER_BOX :
					typeStr = "Shulker Box";
					break;
				default:
					break;
				}
				
				currentReport.add(new LiteralText("Source: "  + typeStr));
				currentReport.add(new LiteralText("Initial Movement: " + movement));
				currentReport.add(new LiteralText("Initial Motion: " + this.velocity));
				lastMovement = movement;
			}
		}
	}
	
	@Redirect(method = "move", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			)
	)
	private Vec3d onPistonMovementRestriction(Entity e, Vec3d movement) {
		Vec3d vec = this.adjustMovementForPiston(movement);
		if(currentReport != null && !this.world.isClient) {
			currentReport.add(new LiteralText("Restricted piston movement to " + vec));
			lastMovement = vec;
		}
		
		return vec;
	}
	
	@Inject(method = "move", 
			at = @At(value = "FIELD", 
					target = "Lnet/minecraft/entity/Entity;movementMultiplier:Lnet/minecraft/util/math/Vec3d;", 
					opcode = Opcodes.PUTFIELD
			)
	)
	private void onCobwebMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !movement.equals(lastMovement) && !this.world.isClient) {
			currentReport.add(new LiteralText("MovementMultipler restricted the movement to " + movement));
			lastMovement = movement;
		}
	}
	
	@Redirect(method = "move", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForSneaking(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;"
			) 
	)
	private Vec3d onSneakingMovementRestriction(Entity e, Vec3d movement, MovementType type) {
		Vec3d vec = this.adjustMovementForSneaking(movement, type);
		if(currentReport != null && !vec.equals(movement) && !this.world.isClient) {
			currentReport.add(new LiteralText("Sneaking restricted the movement to " + vec));
			lastMovement = vec;
		}
		
		return vec;
	}
	
	@Redirect(method = "move", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			)
	)
	private Vec3d onCollisionMovementRestriction(Entity e, Vec3d movement) {
		Vec3d vec = this.adjustMovementForCollisions(movement);
		if(currentReport != null && !vec.equals(movement) && !this.world.isClient) {
			currentReport.add(new LiteralText("Collision restricted the movement to " + vec));
			lastMovement = vec;
		}
		
		return vec;
	}
	
	@Inject(method = "move", 
			at = @At("RETURN"), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void postMove(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !this.world.isClient) {
			currentReport.add(new LiteralText("Final Motion: " + this.velocity));
			currentReport.forEach((t) -> world.getServer().getPlayerManager().broadcast(t, MessageType.CHAT, Util.NIL_UUID));
			currentReport = null;
			lastMovement = new Vec3d(Double.NaN, Double.NaN, Double.NaN);
		}
	}
}
