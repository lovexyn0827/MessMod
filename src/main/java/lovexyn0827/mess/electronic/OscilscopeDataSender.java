package lovexyn0827.mess.electronic;

import java.util.Collection;

import lovexyn0827.mess.util.ServerMicroTime;
import net.minecraft.server.network.ServerPlayerEntity;

interface OscilscopeDataSender {
	void sendTrigger(Oscilscope.Trigger trig);
	void sendEdge(Oscilscope.Channel channel, Oscilscope.Edge edge);
	
	default void sendEdge(Oscilscope.Channel channel, int level) {
		this.sendEdge(channel, new Oscilscope.Edge(ServerMicroTime.current(), level));
	}
	
	void sendNewChannel(Oscilscope.Channel channel);
	void sendChannelsTo(Collection<Oscilscope.Channel> channels, ServerPlayerEntity player);
}