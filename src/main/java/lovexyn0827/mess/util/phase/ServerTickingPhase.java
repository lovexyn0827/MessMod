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
	DIM_REST(false), 
	TICKED_ALL_WORLDS(true), 
	SERVER_TASKS(true), 
	REST(true);
	
	private static ServerTickingPhase current;
	private final List<TickingPhase.Event> events = Lists.newArrayList();
	public final boolean notInAnyWorld;
	
	private ServerTickingPhase(boolean notInAnyWorld) {
		this.notInAnyWorld = notInAnyWorld;
	}
	
	@Override
	public void begin(@Nullable World world) {
		current = this;
		this.triggerEvents(world);
	}

	protected synchronized void triggerEvents(@Nullable World world) {
		this.events.forEach((e) -> e.trigger(this, world));
	}
	
	@Override
	public synchronized void addEvent(TickingPhase.Event event) {
		this.events.add(event);
	}

	@Override
	public synchronized void removeEvent(TickingPhase.Event event) {
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
	
	public static void initialize() {
		current = null;
		for(ServerTickingPhase phase : values()) {
			phase.events.clear();
		}
	}
	
	@Nullable
	public static ServerTickingPhase current() {
		return current;
	}
	
	public static void removeEventFromAll(TickingPhase.Event event) {
		for(ServerTickingPhase phase : values()) {
			phase.removeEvent(event);
		}
	}
}
