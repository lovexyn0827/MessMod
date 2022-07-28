package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	private MinecraftServer server;
	@Shadow
	public ServerPlayerEntity player;
	
	@Redirect(method = {"onDisconnected", "onUpdateDifficulty", "onUpdateDifficultyLock"},
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"
			)
	)
	private boolean originalIsHost(ServerPlayNetworkHandler serverPlayNetworkHandler) {
		return this.server.isHost(this.player.getGameProfile());
	}
	
	@Inject(method = "isHost",
			at = @At("HEAD"), 
			cancellable = true
	)
	private void redirectIsHost(CallbackInfoReturnable<Boolean> cir) {
		if(OptionManager.antiHostCheating) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
