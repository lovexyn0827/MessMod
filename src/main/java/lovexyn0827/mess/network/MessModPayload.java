package lovexyn0827.mess.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// Actually we cannot take advantage of the new custom payload system as we should maintain versions for 1.16.x
public record MessModPayload(Identifier channel, PacketByteBuf data) implements CustomPayload {
	@Override
	public void write(PacketByteBuf bufIn) {
		bufIn.writeBytes(this.data);
	}

	@Override
	public Identifier id() {
		return this.channel;
	}
}
