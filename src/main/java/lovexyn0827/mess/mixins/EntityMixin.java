package lovexyn0827.mess.mixins;

import java.util.List;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Lists;

import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInterface {
	@Shadow private Vec3d pos;
	@Shadow private Vec3d velocity;
	@Shadow private int id;
	@Shadow private World world;
	@Shadow private EntityType<?> type;
	private static List<Text> currentReport;
	private static Vec3d lastMovement;
	private boolean isFrozen;
	private boolean isStepHeightDisabled;
	private boolean shouldLogMovement;
	
	@SuppressWarnings("rawtypes")
	@Inject(at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;getStepHeight()F"
			), 
			cancellable = true,
			method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onCalculatingStepHeight(Vec3d movement, CallbackInfoReturnable<Vec3d> cir, 
			Box box, List list, Vec3d vec3d) {
		if(this.isStepHeightDisabled) {
			cir.setReturnValue(vec3d);
			cir.cancel();
		}
	}
	
	@Inject(method = "move", 
			at = @At("HEAD")
	)
	private void onMoveStart(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(type != MovementType.SELF && type != MovementType.PLAYER) {
			if(this.shouldLogMovement && !this.world.isClient) {
				currentReport = Lists.newArrayList();
				currentReport.add(Text.literal("Tick: " + this.world.getTime()).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
				currentReport.add(Text.literal("Entity: " + this.type + '(' + this.id + ')'));
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
				
				currentReport.add(Text.literal("Source: "  + typeStr));
				currentReport.add(Text.literal("Initial Movement: " + movement));
				currentReport.add(Text.literal("Initial Motion: " + this.velocity));
				lastMovement = movement;
			}
		}
	}
	
	@Inject(method = "move", 
			at = @At(value = "INVOKE_ASSIGN", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onPistonMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !this.world.isClient  && !movement.equals(lastMovement)) {
			currentReport.add(Text.literal("Restricted piston movement to " + movement));
			lastMovement = movement;
		}
	}
	
	@Inject(method = "move", 
			at = @At(value = "FIELD", 
					target = "Lnet/minecraft/entity/Entity;movementMultiplier:Lnet/minecraft/util/math/Vec3d;", 
					opcode = Opcodes.PUTFIELD
			)
	)
	private void onCobwebMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !movement.equals(lastMovement) && !this.world.isClient) {
			currentReport.add(Text.literal("MovementMultipler restricted the movement to " + movement));
			lastMovement = movement;
		}
	}
	
	@Inject(method = "move", 
			at = @At(value = "INVOKE_ASSIGN", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForSneaking(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onSneakingMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !movement.equals(lastMovement) && !this.world.isClient) {
			currentReport.add(Text.literal("Sneaking restricted the movement to " + movement));
			lastMovement = movement;
		}
	}
	
	@Inject(method = "move", 
			at = @At(value = "INVOKE_ASSIGN", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onCollisionMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci, 
			Profiler profiler,  Vec3d vec3d) {
		if(currentReport != null && !vec3d.equals(lastMovement) && !this.world.isClient) {
			currentReport.add(Text.literal("Collision restricted the movement to " + vec3d));
			lastMovement = vec3d;
		}
	}
	
	@Inject(method = "move", 
			at = @At("RETURN")
	)
	private void postMove(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !this.world.isClient) {
			currentReport.add(Text.literal("Final Motion: " + this.velocity));
			currentReport.forEach((t) -> world.getServer().getPlayerManager().broadcast(t, false));
			currentReport = null;
			lastMovement = null;
		}
	}
	
	@Override
	public boolean isFrozen() {
		return this.isFrozen;
	}
	
	@Override
	public boolean isStepHeightDisabled() {
		return this.isStepHeightDisabled;
	}
	
	@Override
	public boolean shouldLogMovement() {
		return this.shouldLogMovement;
	}
	@Override
	public void setFrozen(boolean frozen) {
		this.isFrozen = frozen;
	}
	@Override
	public void setStepHeightDisabled(boolean disabled) {
		this.isStepHeightDisabled = disabled;
	}
	@Override
	public void setMovementSubscribed(boolean subscribed) {
		this.shouldLogMovement = subscribed;
	}
}
