package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.LookingAtEntityHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Final ClientPlayerEntity player;
	@Shadow @Final IntegratedServer server;
	
	@Inject(method = "render",at = @At(value = "CONSTANT",args = "stringValue=blit"))
	public void onRender(boolean tick,CallbackInfo ci) {
		if(this.player!=null&&this.server!=null) {
			LookingAtEntityHud.render(new MatrixStack(),MinecraftClient.getInstance());
		}
	}
	
	@Inject(method = "<init>",at = @At(value = "RETURN"))
	public void debugLoadClass(RunArgs args,CallbackInfo ci) {
		Class<IntegratedServer> class0 = IntegratedServer.class;
	}
}