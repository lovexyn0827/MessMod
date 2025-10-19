package lovexyn0827.mess.electronic;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ServerMicroTime;
import net.minecraft.network.PacketByteBuf;

// TODO @Environment(EnvType.CLIENT)
class RemoteOscilscopeDataStorage extends OscilscopeDataStorage {
	public void handlePacket(PacketByteBuf buf) {
		switch (buf.readByte()) {
		case RemoteOscilscopeDataSender.TRIGGER:
 			Oscilscope.Channel trigCh = MessMod.INSTANCE.getOscilscope().getChannel(buf.readInt());
			ServerMicroTime trigTime = ServerMicroTime.read(buf);
			Oscilscope.Trigger trig = new Oscilscope.Trigger(trigCh, trigTime, buf.readBoolean());
 			if (trigCh == null) {
 				return;
 			}
 			
			this.addTrigger(trig);
			break;
		case RemoteOscilscopeDataSender.EDGE:
			Oscilscope.Channel edgeCh = MessMod.INSTANCE.getOscilscope().getChannel(buf.readInt());
			ServerMicroTime time = ServerMicroTime.read(buf);
			Oscilscope.Edge edge = new Oscilscope.Edge(time, buf.readInt());
 			if (edgeCh == null) {
 				return;
 			}
 			
			this.addEdge(edgeCh, edge);
			break;
		case RemoteOscilscopeDataSender.CHANNEL:
			Oscilscope.Channel newCh = MessMod.INSTANCE.getOscilscope().deserializeChannel(buf.readNbt());
			this.addChannel(newCh);
			break;
		}
	}
}
