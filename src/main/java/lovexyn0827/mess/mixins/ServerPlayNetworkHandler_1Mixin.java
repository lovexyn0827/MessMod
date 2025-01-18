package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$1")
public class ServerPlayNetworkHandler_1Mixin {
	@Shadow(remap = false)
	private @Final ServerPlayNetworkHandler field_28963;
	@Shadow(remap = false)
	private @Final net.minecraft.entity.Entity field_28962;
	
	@Inject(
			method = "attack", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/server/network/ServerPlayerEntity.attack(Lnet/minecraft/entity/Entity;)V", 
					shift = At.Shift.AFTER
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void tryKillStackedEntities(CallbackInfo ci) {
		if(OptionManager.quickStackedEntityKilling && this.field_28963.player.getMainHandStack().getItem() == Items.BRICK) {
			int count = 0;
			Entity entity = this.field_28962;
			for(Entity e : entity.getWorld().getOtherEntities(null, entity.getBoundingBox().expand(10E-3))) {
				if(e.getPos().equals(entity.getPos())) {
					if(OptionManager.mobFastKill) {
						e.remove(RemovalReason.KILLED);
					} else {
						e.kill((ServerWorld) e.getWorld());
					}
					
					count++;
				}
			}
			
			// Finally we can use Bugjump's translation
			this.field_28963.player.sendMessage(Text.translatable("commands.kill.success.multiple", count), false);
		}
	}
}
