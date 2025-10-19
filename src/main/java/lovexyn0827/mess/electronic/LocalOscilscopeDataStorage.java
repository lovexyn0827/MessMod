package lovexyn0827.mess.electronic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.server.network.ServerPlayerEntity;

class LocalOscilscopeDataStorage extends OscilscopeDataStorage implements OscilscopeDataSender {
	@Override
	public synchronized Map<Oscilscope.Channel, List<Oscilscope.Edge>>
			getWaveData(long fromTick, long toTick, boolean digitalMode) {
		return super.getWaveData(fromTick, toTick, digitalMode);
	}
	
	@Override
	public void sendTrigger(Oscilscope.Trigger trig) {
		this.addTrigger(trig);
	}

	@Override
	public synchronized void sendEdge(Oscilscope.Channel channel, Oscilscope.Edge edge) {
		this.addEdge(channel, edge);
	}
	
	@Override
	public void sendNewChannel(Oscilscope.Channel newChannel) {
		this.addChannel(newChannel);
	}

	@Override
	public void sendChannelsTo(Collection<Oscilscope.Channel> channels, ServerPlayerEntity player) {
		// No action is needed as all channels are stores locally, and thus no sending is necessary
	}
}
