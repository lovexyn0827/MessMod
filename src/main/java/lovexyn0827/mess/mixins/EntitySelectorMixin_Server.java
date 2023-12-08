package lovexyn0827.mess.mixins;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import lovexyn0827.mess.fakes.EntitySelectorInterface;
import lovexyn0827.mess.util.RaycastUtil;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin_Server implements EntitySelectorInterface {
	private boolean targetOnly;
	
	@Shadow
	protected abstract void checkSourcePermission(ServerCommandSource serverCommandSource);

	@Shadow
	private <T extends Entity> List<T> getEntities(Vec3d vec3d, List<Entity> list) {
		throw new AssertionError();
	}
	
	@Inject(method = "getEntities(Lnet/minecraft/server/command/ServerCommandSource;)Ljava/util/List;", 
			at = @At("HEAD"), 
			cancellable = true
	)
	private void selectTarget(ServerCommandSource serverCommandSource, 
			CallbackInfoReturnable<List<? extends Entity>> cir) {
		if(this.targetOnly) {
			this.checkSourcePermission(serverCommandSource);
			List<Entity> result = Lists.newArrayList();
			Entity senderer = serverCommandSource.getEntity();
			if(senderer != null) {
				Entity target = RaycastUtil.getTargetEntity(senderer);
				if(target != null) {
					result.add(target);
				}
			}
			
			cir.setReturnValue(result);
			cir.cancel();
		}
	}

	@Override
	public void setTargetOnly(boolean targetOnly) {
		this.targetOnly = targetOnly;
	}
	
	@Override
	public void setSide(NetworkSide side) {
		return;
	}
}
