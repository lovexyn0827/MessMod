package lovexyn0827.mess.mixins;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.LogPacketCommand;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	@Shadow
	private NetworkSide side;
	
	@Inject(method = "sendImmediately", at = @At("HEAD"))
	private void onPacketBeingSended(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
		if(LogPacketCommand.isSubscribed(packet)) {
			MessMod.LOGGER.info("{}: Sended Packet: {}", 
					this.side == NetworkSide.CLIENTBOUND ? "CLIENT" : "SERVER", 
					toString(packet));
		}
	}
	
	@Inject(method = "channelRead0", at = @At("HEAD"))
	private void onPacketBeingSended(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
		if(LogPacketCommand.isSubscribed(packet)) {
			MessMod.LOGGER.info("{}: Received Packet: {}", 
					this.side == NetworkSide.CLIENTBOUND ? "CLIENT" : "SERVER", 
					toString(packet));
		}
	}
	
	private static String toString(Packet<?> packet) {
		Mapping mapping = MessMod.INSTANCE.getMapping();
		StringBuilder sb = new StringBuilder(mapping.simpleNamedClass(packet.getClass().getName()));
		sb.append('[');
		for(Field f : packet.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				sb.append(mapping.namedField(f.getName())).append('=').append(f.get(packet)).append(',');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		sb.append(']');
		return sb.toString();
	}
}
