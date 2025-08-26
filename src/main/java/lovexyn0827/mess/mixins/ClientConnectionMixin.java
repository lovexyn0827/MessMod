package lovexyn0827.mess.mixins;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lovexyn0827.mess.command.LogPacketCommand;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	@Shadow
	private NetworkSide side;
	
	@Inject(method = "sendImmediately", at = @At("HEAD"))
	private void onPacketBeingSended(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
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
