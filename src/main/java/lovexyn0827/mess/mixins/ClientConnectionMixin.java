package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import lovexyn0827.mess.command.LogPacketCommand;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	@Shadow
	private NetworkSide side;
	
	@Inject(method = "sendImmediately", at = @At("HEAD"))
	private void onPacketBeingSended(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
		if(LogPacketCommand.isSubscribed(packet)) {
			LogPacketCommand.onPacket(this.side, packet, true);
		}
	}
	
	@Inject(method = "channelRead0", at = @At("HEAD"))
	private void onPacketBeingSended(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
		if(LogPacketCommand.isSubscribed(packet)) {
			LogPacketCommand.onPacket(this.side, packet, false);
		}
	}
}
