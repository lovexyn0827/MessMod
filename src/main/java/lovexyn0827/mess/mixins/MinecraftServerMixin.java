package lovexyn0827.mess.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.phase.ServerTickingPhase;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	public void onTickStart(BooleanSupplier bs, CallbackInfo ci) {
		// Actually here is before WTU and thus +1 is necessary for consistence
		MessMod.INSTANCE.updateTime(((MinecraftServer)(Object) this).getOverworld().getTime() + 1);
	}
	
	@Inject(method = "tick", at = @At(value = "RETURN"))
	public void onTicked(BooleanSupplier bs, CallbackInfo ci) {
		ServerTickingPhase.TICKED_ALL_WORLDS.begin(null);
		MessMod.INSTANCE.onServerTicked((MinecraftServer)(Object) this);
	}
	
	@Inject(method = "runServer",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z",
					shift = At.Shift.AFTER
			)
	)
	private void onServerStarted(CallbackInfo ci) {
		MessMod.INSTANCE.onServerStarted((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "shutdown",at = @At(value = "RETURN"))
	private void onServerShutdown(CallbackInfo ci) {
		MessMod.INSTANCE.onServerShutdown((MinecraftServer)(Object)this);
	}
	
	@Inject(method = "method_16208",at = @At(value = "HEAD"))
	private void onAsyncTasksBegin(CallbackInfo ci) {
		ServerTickingPhase.SERVER_TASKS.begin(null);
	}
	
	@Inject(method = "method_16208",at = @At(value = "RETURN"))
	private void onAsyncTasksExecuted(CallbackInfo ci) {
		ServerTickingPhase.REST.begin(null);
	}
}
