package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.MessServerNetworkHandler;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
	@Shadow
	private MinecraftServer server;
	
	@Shadow
	protected abstract GameProfile getProfile();
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		MessServerNetworkHandler handler = MessMod.INSTANCE.getServerNetworkHandler();
		ServerPlayerEntity player = this.getPlayerIfAvailable();
		if (handler != null && player != null && handler.handlePacket(packet, player)) {
			ci.cancel();
		}
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

	private ServerPlayerEntity getPlayerIfAvailable() {
		if((Object) this instanceof ServerPlayNetworkHandler) {
			return ((ServerPlayNetworkHandler)(Object) this).getPlayer();
		} else {
			return null;
		}
	}
	
	@Redirect(method = "onDisconnected",
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/server/network/ServerCommonNetworkHandler;isHost()Z"
			)
	)
	private boolean originalIsHost(ServerCommonNetworkHandler serverPlayNetworkHandler) {
		return this.server.isHost(this.getProfile());
	}
}
