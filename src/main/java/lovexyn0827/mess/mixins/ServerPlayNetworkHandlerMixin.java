package lovexyn0827.mess.mixins;

import java.util.Optional;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.network.MessServerNetworkHandler;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.HudType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements HudDataSubscribeState {
	@Shadow
	private MinecraftServer server;
	@Shadow
	public ServerPlayerEntity player;
	public Set<HudType> subscribedHuds = Sets.newHashSet();
	
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

	@Override
	public boolean isSubscribed(HudType type) {
		return this.subscribedHuds.contains(type);
	}

	@Override
	public void subscribe(HudType type) {
		this.subscribedHuds.add(type);
	}

	@Override
	public void unsubscribe(HudType type) {
		this.subscribedHuds.remove(type);
	}
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		MessServerNetworkHandler handler = MessMod.INSTANCE.getServerNetworkHandler();
		if (handler != null && handler.handlePacket(packet, player)) {
			ci.cancel();
		}
	}
}
