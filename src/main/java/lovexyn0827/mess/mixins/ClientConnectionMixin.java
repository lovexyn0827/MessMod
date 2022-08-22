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
import lovexyn0827.mess.MessMod;
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
			MessMod.LOGGER.info("{}: Sended Packet: {} [{}]", 
					this.side == NetworkSide.CLIENTBOUND ? "CLIENT" : "SERVER", 
					packet.getClass().getSimpleName(), 
					getFields(packet));
		}
	}
	
	@Inject(method = "channelRead0", at = @At("HEAD"))
	private void onPacketBeingSended(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
		if(LogPacketCommand.isSubscribed(packet)) {
			MessMod.LOGGER.info("{}: Received Packet: {} [{}]", 
					this.side == NetworkSide.CLIENTBOUND ? "CLIENT" : "SERVER", 
					packet.getClass().getSimpleName(), 
					getFields(packet));
		}
	}
	
	private static String getFields(Packet<?> packet) {
		StringBuilder sb = new StringBuilder();
		for(Field f : packet.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				sb.append(f.getName()).append('=').append(f.get(packet)).append(',');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
}
