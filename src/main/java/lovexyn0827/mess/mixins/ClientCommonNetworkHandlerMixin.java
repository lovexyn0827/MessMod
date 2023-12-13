package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.MessClientNetworkHandler;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		MessClientNetworkHandler handler = MessMod.INSTANCE.getClientNetworkHandler();
		if(handler != null) {
			if (MessMod.INSTANCE.getClientNetworkHandler().handlePacket(packet)) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "onDisconnect",at = @At(value = "RETURN"))
	private void onPlayerDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
		MessMod.INSTANCE.onDisconnect(packet);
	}

}
