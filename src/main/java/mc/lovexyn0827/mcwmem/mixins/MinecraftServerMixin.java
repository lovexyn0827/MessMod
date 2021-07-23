package mc.lovexyn0827.mcwmem.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.MCWMEMod;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Inject(method = "tick",at = @At(value = "RETURN"))
	public void onTicked(BooleanSupplier bs,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onServerTicked((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "runServer",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z",
					shift = At.Shift.AFTER
			)
	)
	public void onServerStarted(CallbackInfo ci) {
		MCWMEMod.INSTANCE.onServerStarted((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "shutdown",at = @At(value = "RETURN"))
	public void onServerShutdown(CallbackInfo ci) {
		MCWMEMod.INSTANCE.onServerShutdown((MinecraftServer)(Object)this);
	}
}
