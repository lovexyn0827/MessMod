package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onGameJoin",at = @At(value = "RETURN"))
	public void onGameJoined(GameJoinS2CPacket packet,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onGameJoined(packet);
	}
	
	@Inject(method = "onPlayerRespawn",at = @At(value = "RETURN"))
	public void onPlayerRespawned(PlayerRespawnS2CPacket packet,CallbackInfo ci) {
		MCWMEMod.INSTANCE.onPlayerRespawned(packet);
	}
}
