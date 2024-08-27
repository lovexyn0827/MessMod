package lovexyn0827.mess.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.command.LogPacketCommand;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkStateBuilder;

@Mixin(NetworkStateBuilder.class)
public class NetworkStateBuilderMixin {
	@Shadow
	@Final
	private List<NetworkStateBuilder.PacketType<?, ?, ?>> packetTypes;
	
	@Inject(method = { "buildFactory" }, at = @At("HEAD"))
	private void capturePacketTypes(CallbackInfoReturnable<NetworkState.Factory<?, ?>> cir) {
		this.packetTypes.stream().map((t) -> t.id().id()).forEach(LogPacketCommand.PACKET_TYPES::add);
	}
}
