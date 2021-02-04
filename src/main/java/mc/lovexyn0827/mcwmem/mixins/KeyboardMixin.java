package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mc.lovexyn0827.mcwmem.hud.ClientPlayerHud;
import mc.lovexyn0827.mcwmem.hud.LookingAtEntityHud;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Shadow @Final MinecraftClient client;
	
	@Inject(method = "processF3",at = @At(value = "HEAD"))
	public void onF3Pressed(int key,CallbackInfoReturnable<?> ci) {
		if(key==69) {
			LookingAtEntityHud.shouldRender ^= true;
			this.client.player.sendChatMessage("Looking At Entity HUD:"+(LookingAtEntityHud.shouldRender?"On":"Off"));
		}else if(key==77) {
			ClientPlayerHud.shouldRender^=true;
			this.client.player.sendChatMessage("Client Player Information HUD:"+(ClientPlayerHud.shouldRender?"On":"Off"));
		}
	}
}
