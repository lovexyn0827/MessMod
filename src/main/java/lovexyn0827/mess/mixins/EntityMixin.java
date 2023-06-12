package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.stream.Stream;

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
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.collection.ReusableStream;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInterface {
	@Shadow private Vec3d pos;
	@Shadow private Vec3d velocity;
	@Shadow private int entityId;
	@Shadow private World world;
	@Shadow private EntityType<?> type;
	private static List<Text> currentReport;
	private static Vec3d lastMovement;
	private boolean isFrozen;
	private boolean isStepHeightDisabled;
	private boolean shouldLogMovement;
	
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
	private void onCalculatingStepHeight(Vec3d movement, 
			CallbackInfoReturnable<Vec3d> ci, 
			Box box, 
			ShapeContext shapeContext, 
			Stream stream, Stream stream2, 
			ReusableStream reusableStream, 
			Vec3d vec3d) {
		if(this.isStepHeightDisabled) {
			ci.setReturnValue(vec3d);
			ci.cancel();
		}
	}
	
	@Inject(method = "move", 
			at = @At("HEAD")
	)
	private void onMoveStart(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(type != MovementType.SELF && type != MovementType.PLAYER) {
			if(this.shouldLogMovement && !this.world.isClient) {
				currentReport = Lists.newArrayList();
				currentReport.add(new LiteralText("Tick: " + this.world.getTime()).formatted(Formatting.DARK_GREEN, Formatting.BOLD));
				currentReport.add(new LiteralText("Entity: " + this.type + '(' + this.entityId + ')'));
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
	
	@Inject(method = "move", 
			at = @At(value = "INVOKE_ASSIGN", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onPistonMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !this.world.isClient  && !movement.equals(lastMovement)) {
			currentReport.add(new LiteralText("Restricted piston movement to " + movement));
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
			currentReport.add(new LiteralText("MovementMultipler restricted the movement to " + movement));
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
			currentReport.add(new LiteralText("Sneaking restricted the movement to " + movement));
			lastMovement = movement;
		}
	}
	
	@Inject(method = "move", 
			at = @At(value = "INVOKE_ASSIGN", 
					target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onCollisionMovementRestriction(MovementType type, Vec3d movement, CallbackInfo ci, Vec3d vec3d) {
		if(currentReport != null && !vec3d.equals(lastMovement) && !this.world.isClient) {
			currentReport.add(new LiteralText("Collision restricted the movement to " + movement));
			lastMovement = vec3d;
		}
	}
	
	@Inject(method = "move", 
			at = @At("RETURN")
	)
	private void postMove(MovementType type, Vec3d movement, CallbackInfo ci) {
		if(currentReport != null && !this.world.isClient) {
			currentReport.add(new LiteralText("Final Motion: " + this.velocity));
			currentReport.forEach((t) -> world.getServer().getPlayerManager().broadcastChatMessage(t, MessageType.CHAT, Util.NIL_UUID));
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
