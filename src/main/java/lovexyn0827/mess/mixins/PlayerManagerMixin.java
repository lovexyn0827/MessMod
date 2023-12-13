package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
	private void onServerPlayerSpawned(ClientConnection connection, ServerPlayerEntity player, 
			ConnectedClientData clientData, CallbackInfo ci) {
		MessMod.INSTANCE.onServerPlayerSpawned(player);
	}
}
