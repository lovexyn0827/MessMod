package mc.lovexyn0827.mcwmem.mixins;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.server.integrated.IntegratedServer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow @Final ClientPlayerEntity player;
	@Shadow @Final IntegratedServer server;
	
	@Shadow abstract Window getWindow();
	
	@Inject(method = "render",at = @At(value = "CONSTANT",args = "stringValue=blit"))
	public void onRender(boolean tick,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onRender(this.player,this.server);
	}
	
	@Inject(method = "tick",at = @At(value = "RETURN"))
	public void onTick(CallbackInfo ci) {
		MCWMEMod.INSTANCE.onClientTicked();
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
			at = @At(value = "HEAD"))
	public void onDisconnected(Screen screen,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onDisconnected();
	}
}