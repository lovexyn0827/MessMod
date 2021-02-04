package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.hud.ClientPlayerHud;
import mc.lovexyn0827.mcwmem.hud.LookingAtEntityHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Final ClientPlayerEntity player;
	@Shadow @Final IntegratedServer server;
	
	@Inject(method = "render",at = @At(value = "CONSTANT",args = "stringValue=blit"))
	public void onRender(boolean tick,CallbackInfo ci) {
		int y = 10;
		if(this.player!=null&&this.server!=null) {
			if(LookingAtEntityHud.shouldRender) y = LookingAtEntityHud.render(10);
			if(ClientPlayerHud.shouldRender) ClientPlayerHud.render(y);
		}
	}
	
	@Inject(method = "tick",at = @At(value = "RETURN"))
	public void onTick(CallbackInfo ci) {
		ClientPlayerHud.updateData();
	}
}