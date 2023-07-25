package lovexyn0827.mess.util.phase;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.world.World;

public enum ClientTickingPhase implements TickingPhase {
	CLIENT_TICK_START, 
	CLIENT_TICK_END;

	private final List<TickingPhase.Event> events = Lists.newArrayList();
	
	@Override
	public void begin(@Nullable World world) {
		this.triggerEvents(world);
	}
	
	protected void triggerEvents(@Nullable World world) {
		this.events.forEach((e) -> e.trigger(this, world));
	}

	@Override
	public void addEvent(TickingPhase.Event event) {
		this.events.add(event);
	}

	@Override
	public void removeEvent(TickingPhase.Event event) {
		this.events.remove(event);
	}

	@Override
	public boolean isNotInAnyWorld() {
		return true;
	}
	
	public static void addEventToAll(TickingPhase.Event event) {
		for(ClientTickingPhase phase : values()) {
			phase.addEvent(event);
		}
	}
	
	public static void removeAllEvents() {
		for(ClientTickingPhase phase : values()) {
			phase.events.clear();
		}
	}
}
