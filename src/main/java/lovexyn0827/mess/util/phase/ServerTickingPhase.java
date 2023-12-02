package lovexyn0827.mess.util.phase;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.world.World;

public enum ServerTickingPhase implements TickingPhase {
	WEATHER_CYCLE(false),
	CHUNK(false),
	SCHEDULED_TICK(false),
	VILLAGE(false),
	BLOCK_EVENT(false),
	ENTITY(false),
	TILE_ENTITY(false),
	TICKED_ALL_WORLDS(true), 
	SERVER_TASKS(true);
	
	private final List<TickingPhase.Event> events = Lists.newArrayList();
	public final boolean notInAnyWorld;
	
	private ServerTickingPhase(boolean notInAnyWorld) {
		this.notInAnyWorld = notInAnyWorld;
	}
	
	public void triggerEvents(@Nullable World world) {
		this.events.forEach((e) -> e.trigger(this, world));
	}
	
	public void addEvent(TickingPhase.Event event) {
		this.events.add(event);
	}

	public void removeEvent(TickingPhase.Event event) {
		this.events.remove(event);
	}

	@Override
	public boolean isNotInAnyWorld() {
		return this.notInAnyWorld;
	}

	public static void addEventToAll(TickingPhase.Event event) {
		for(ServerTickingPhase phase : values()) {
			phase.addEvent(event);
		}
	}
	
	public static void removeAllEvents() {
		for(ServerTickingPhase phase : values()) {
			phase.events.clear();
		}
	}
}
