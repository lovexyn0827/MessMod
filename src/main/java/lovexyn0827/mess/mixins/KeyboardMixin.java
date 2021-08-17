package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.LookingAtEntityHud;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
	@Shadow @Final MinecraftClient client;
	
	@Inject(method = "processF3",at = @At(value = "HEAD"))
	public void onF3Pressed(int key,CallbackInfoReturnable<?> ci) {
		if(key==69) {
			LookingAtEntityHud lookingHud = MessMod.INSTANCE.hudManager.lookingHud;
			if(lookingHud!=null) lookingHud.toggleRender();;
			this.client.player.sendChatMessage("Looking At Entity HUD:"+(lookingHud.shouldRender?"On":"Off"));
		}else if(key==77) {
			PlayerHud playerHud = MessMod.INSTANCE.hudManager.playerHudC;
			if(playerHud!=null) playerHud.toggleRender();
			this.client.player.sendChatMessage("Client Player Information HUD:"+(playerHud.shouldRender?"On":"Off"));
		} else if(key == 'S') {
			PlayerHud playerHud = MessMod.INSTANCE.hudManager.playerHudS;
			if(playerHud ==null ) return;
			playerHud.toggleRender();
			this.client.player.sendChatMessage("Server Player Information HUD:"+(playerHud.shouldRender?"On":"Off"));
		}
	}
}
