package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onGameJoin",at = @At(value = "RETURN"))
	public void onGameJoined(GameJoinS2CPacket packet,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onGameJoined(packet);
	}
}
