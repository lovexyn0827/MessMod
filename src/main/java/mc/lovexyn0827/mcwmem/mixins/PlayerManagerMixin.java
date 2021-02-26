package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
	public void onServerPlayerSpawned(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		MCWMEMod.INSTANCE.onServerPlayerSpawned(player);
	}
}
