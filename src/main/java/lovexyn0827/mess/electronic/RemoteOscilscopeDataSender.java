package lovexyn0827.mess.electronic;

import java.util.Collection;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.network.MessModPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class RemoteOscilscopeDataSender implements OscilscopeDataSender {
	static final byte TRIGGER = 0;
	static final byte EDGE = 1;
	static final byte CHANNEL = 2;
	
	private void sendPacket(byte action, Consumer<PacketByteBuf> writer) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(action);
		writer.accept(buf);
		CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(
				new MessModPayload(Channels.OSCILSCOPE, buf));
		MessMod.INSTANCE.getServerNetworkHandler().sendToEveryone(pkt);	// XXX Selective?
	}
	
	@Override
	public void sendTrigger(Oscilscope.Trigger trig) {
		this.sendPacket(TRIGGER, (buf) -> {
			buf.writeInt(trig.channel.getId());
			trig.time.write(buf);
			buf.writeBoolean(trig.rising);
		});
	}

	@Override
	public void sendEdge(Oscilscope.Channel channel, Oscilscope.Edge edge) {
		this.sendPacket(EDGE, (buf) -> {
			buf.writeInt(channel.getId());
			edge.time.write(buf);
			buf.writeInt(edge.newLevel);
		});
	}

	@Override
	public void sendNewChannel(Oscilscope.Channel ch) {
		this.sendPacket(CHANNEL, (buf) -> {
			buf.writeNbt(ch.toTag());
		});
	}

	@Override
	public void sendChannelsTo(Collection<Oscilscope.Channel> channels, ServerPlayerEntity player) {
		channels.forEach((ch) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(CHANNEL);
			buf.writeNbt(ch.toTag());
			CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(
					new MessModPayload(Channels.OSCILSCOPE, buf));
			MessMod.INSTANCE.getServerNetworkHandler().sendPacketTo(pkt, player);
		});
	}
}
