package lovexyn0827.mess.electronic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ServerMicroTime;

abstract class OscilscopeDataStorage {
	// Using LinkedList brings about ~1.5X heap overhead, but fine since it make it possible to purge old edges
	private final Map<Oscilscope.Channel, LinkedList<Oscilscope.Edge>> waveData = new HashMap<>();
	private final ConcurrentLinkedDeque<Oscilscope.Trigger> newTriggers = new ConcurrentLinkedDeque<>();
	private final LinkedList<Oscilscope.Trigger> triggers = new LinkedList<>();
	private final ConcurrentLinkedQueue<Oscilscope.Channel> newChannels = new ConcurrentLinkedQueue<>();
	private final List<Oscilscope.Channel> channels = new ArrayList<>();
	private int storageDepth;
	
	static OscilscopeDataStorage get() {
		return MessMod.INSTANCE.getOscilscope().getDataStorage();
	}

	protected Map<Oscilscope.Channel, LinkedList<Oscilscope.Edge>> getWaveData() {
		return this.waveData;
	}

	public void takeUnprocessedTriggers(Consumer<Oscilscope.Trigger> op) {
		while (!this.newTriggers.isEmpty()) {
			op.accept(this.newTriggers.poll());
		}
	}

	public void takeNewChannels(Consumer<Oscilscope.Channel> op) {
		while (!this.newChannels.isEmpty()) {
			op.accept(this.newChannels.poll());
		}
	}
	
	private final void purge() {
		if (this.storageDepth <= 0) {
			return;
		}
		
		Map<Oscilscope.Channel, ServerMicroTime> lowerBounds = new HashMap<>();
		this.waveData.forEach((ch, wave) -> {
			while (wave.size() > this.storageDepth) {
				wave.removeFirst();
			}
			
			ServerMicroTime oldestEdgeTime = wave.getFirst().time;
			lowerBounds.put(ch, oldestEdgeTime);
		});
		purgeTriggers(this.triggers, lowerBounds);
		purgeTriggers(this.newTriggers, lowerBounds);
	}

	private void purgeTriggers(Deque<Oscilscope.Trigger> triggers, 
			Map<Oscilscope.Channel, ServerMicroTime> lowerBounds) {
		for (Iterator<Oscilscope.Trigger> itr = triggers.iterator(); itr.hasNext();) {
			Oscilscope.Trigger head = itr.next();
			if (head.time.compareTo(lowerBounds.getOrDefault(head.channel, ServerMicroTime.PRE_HISTORY)) < 0) {
				itr.remove();
			} else {
				lowerBounds.remove(head.channel);
			}
			
			if (lowerBounds.isEmpty()) {
				break;
			}
		}
	}

	public void reset() {
		this.waveData.clear();
		this.triggers.clear();
	}
	
	protected void addTrigger(Oscilscope.Trigger trig) {
		this.triggers.add(trig);
		this.newTriggers.add(trig);
	}
	
	protected void addChannel(Oscilscope.Channel channel) {
		this.channels.add(channel);
		this.newChannels.add(channel);
	}
	
	protected void addEdge(Oscilscope.Channel channel, Oscilscope.Edge edge) {
		this.waveData.computeIfAbsent(channel, (k) -> new LinkedList<>()).add(edge);
		this.purge();	// XXX: purge in anther thread
	}

	public List<Oscilscope.Trigger> getTriggerHistory() {
		return this.triggers;
	}

	public List<Oscilscope.Channel> getAllChannels() {
		return this.channels;
	}
	
	// TODO Extract as utility
	private static int findEndPos(List<Oscilscope.Edge> all, long bound, boolean findLast) {
		int left = 0;
		int right = all.size() - 1;
		int candidate = -1;
		if (findLast) {
			while (left <= right) {
				int mid = (left + right) / 2;
				Oscilscope.Edge midElem = all.get(mid);
				if (midElem.time.gameTime <= bound) {
					candidate = mid;
					left = mid + 1;
				} else {
					right = mid - 1;
				}
			}
		} else {
			while (left <= right) {
				int mid = (left + right) / 2;
				Oscilscope.Edge midElem = all.get(mid);
				if (midElem.time.gameTime >= bound) {
					candidate = mid;
					right = mid - 1;
				} else {
					left = mid + 1;
				}
			}
		}
		
		
		return candidate;
	}
	
	Map<Oscilscope.Channel, List<Oscilscope.Edge>> getWaveData(long fromTick, long toTick, boolean digitalMode) {
		Comparator<Oscilscope.Channel> cmp = Comparator.comparingInt(Oscilscope.Channel::getId);
		Map<Oscilscope.Channel, List<Oscilscope.Edge>> result = new TreeMap<>(cmp);
		this.getWaveData().forEach((channel, data) -> {
			if (!channel.isVisible() || data.isEmpty()) {
				return;
			}
			
			int from = findEndPos(data, fromTick, false);
			int to = findEndPos(data, toTick, true);
			if (to == -1) {
				return;
			}
			
			List<Oscilscope.Edge> dataWithinRange = from == -1 ? Collections.emptyList() : data.subList(from, to + 1);
			List<Oscilscope.Edge> clipedData = new ArrayList<>();
			// XXX: Special handling when digitalMode is true?
			if (from == 0) {
				// Got some edges, but no wave exists before found ones
				clipedData.add(new Oscilscope.Edge(ServerMicroTime.PRE_HISTORY, 0));
			} else if (from == -1) {
				// The whole wave is "before" the displayed range
				// Finding its final edge as initial state
				clipedData.add(data.get(data.size() - 1));
			} else {
				// Normal
				if (digitalMode) {
					// Make its time invalid
					clipedData.add(new Oscilscope.Edge(ServerMicroTime.PRE_HISTORY, data.get(from - 1).newLevel));
				} else {
					clipedData.add(data.get(from - 1));
				}
			}
			
			if (digitalMode) {
				Oscilscope.Edge prev = clipedData.get(0);
				for (Oscilscope.Edge edge : dataWithinRange) {
					if (prev.newLevel > 0 ^ edge.newLevel > 0) {
						clipedData.add(edge);
						prev = edge;
					}
				}
			} else {
				clipedData.addAll(dataWithinRange);
			}
			
			result.put(channel, clipedData);
		});
		return result;
	}
	
	void setStorageDepth(int depth) {
		this.storageDepth = depth;
	}
	
	int getStorageDepth() {
		return this.storageDepth;
	}

}