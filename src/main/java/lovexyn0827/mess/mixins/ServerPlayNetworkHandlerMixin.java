package lovexyn0827.mess.mixins;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.rendering.hud.HudType;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements HudDataSubscribeState {
	public Set<HudType> subscribedHuds = Sets.newHashSet();
	@Shadow
	public ServerPlayerEntity player;
	
	@Redirect(method = {"onUpdateDifficulty", "onUpdateDifficultyLock"},
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z"
			)
	)
	private boolean originalIsHost(ServerPlayNetworkHandler serverPlayNetworkHandler) {
		return this.player.server.isHost(this.player.getGameProfile());
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
	private void tryReadMessModPacket(CustomPayloadC2SPacket packet, 
			CallbackInfo ci) {
		MessMod.INSTANCE.getServerNetworkHandler().handlePacket(packet, this.player);
	}
}
