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
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
	@Shadow @Final MinecraftClient client;
	
	@Inject(method = "processF3", at = @At(value = "HEAD"))
	private void onF3Pressed(int key, CallbackInfoReturnable<?> ci) {
		if(key == 'E') {
			LookingAtEntityHud lookingHud = MessMod.INSTANCE.getClientHudManager().lookingHud;
			if(lookingHud != null) lookingHud.toggleRender();;
			this.client.player.sendChatMessage(I18N.translate("hud.target") + (lookingHud.shouldRender ? "On" : "Off"));
		}else if(key == 'M') {
			PlayerHud playerHud = MessMod.INSTANCE.getClientHudManager().playerHudC;
			if(playerHud!=null) playerHud.toggleRender();
			this.client.player.sendChatMessage(I18N.translate("hud.client") + (playerHud.shouldRender ? "On" : "Off"));
		} else if(key == 'S') {
			PlayerHud playerHud = MessMod.INSTANCE.getClientHudManager().playerHudS;
			if(playerHud == null ) return;
			playerHud.toggleRender();
			this.client.player.sendChatMessage(I18N.translate("hud.server") + (playerHud.shouldRender ? "On" : "Off"));
		}
	}
}
