package lovexyn0827.mess.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// Actually we cannot take advantage of the new custom payload system as we should port from 1.16.x
public record MessModPayload(Identifier channel, PacketByteBuf data) implements CustomPayload {
	public static final Id<MessModPayload> ID = new Id<>(new Identifier("messmod", "payload_id"));
	
	public static final PacketCodec<PacketByteBuf, MessModPayload> CODEC = PacketCodec.ofStatic(
			(a, b) -> {
				a.writeIdentifier(b.channel);
				a.writeInt(b.data.readableBytes());
				a.writeBytes(b.data);
			}, 
			(a) -> {
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				Identifier channel = a.readIdentifier();
				a.readBytes(buf, a.readInt());
				return new MessModPayload(channel, buf);
			});

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
