package mc.lovexyn0827.mcwmem.mixins;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mc.lovexyn0827.mcwmem.hud.LookingAtEntityHud;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(MinecraftServer.class)
public abstract class IntegratedServerMixin {	
	@Shadow @Final abstract PlayerManager getPlayerManager();
	
	@Inject(method = "tick",at = @At(value = "RETURN"))
	public void updateHud(BooleanSupplier bs,CallbackInfo ci) {
		ServerPlayerEntity player = this.getPlayerManager().getPlayer(MinecraftClient.getInstance().getSession().getUsername());
		if(player!=null)LookingAtEntityHud.updateData(player);
	}
}
