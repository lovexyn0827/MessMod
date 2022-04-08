package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onGameJoin",at = @At(value = "RETURN"))
	private void onGameJoined(GameJoinS2CPacket packet,CallbackInfo ci) {
		MessMod.INSTANCE.onGameJoined(packet);
	}
	
	@Inject(method = "onPlayerRespawn",at = @At(value = "RETURN"))
	private void onPlayerRespawned(PlayerRespawnS2CPacket packet,CallbackInfo ci) {
		MessMod.INSTANCE.onPlayerRespawned(packet);
	}
}
