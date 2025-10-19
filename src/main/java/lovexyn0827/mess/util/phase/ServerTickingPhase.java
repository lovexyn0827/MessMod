package lovexyn0827.mess.util.phase;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.minecraft.world.World;

public enum ServerTickingPhase implements TickingPhase {
	WEATHER_CYCLE(false, "WTU"),
	CHUNK(false, "CU"),
	SCHEDULED_TICK(false, "NTE"),
	VILLAGE(false, "RAID"),
	BLOCK_EVENT(false, "BE"),
	ENTITY(false, "EU"),
	TILE_ENTITY(false, "TE"),
	DIM_REST(false, "END"), 
	TICKED_ALL_WORLDS(true, "TAW"), 
	SERVER_TASKS(true, "NU"), 
	REST(true, "TEND");
	
	private static final ImmutableMap<String, ServerTickingPhase> PHASES_BY_ABBR;
	private static ServerTickingPhase current;
	private final List<TickingPhase.Event> events = Lists.newCopyOnWriteArrayList();
	public final boolean notInAnyWorld;
	private final String abbreviation;
	
	private ServerTickingPhase(boolean notInAnyWorld, String abbr) {
		this.notInAnyWorld = notInAnyWorld;
		this.abbreviation = abbr;
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
	
	public static ServerTickingPhase byNameOrAbbreviation(String name) {
		if (PHASES_BY_ABBR.containsKey(name)) {
			return PHASES_BY_ABBR.get(name);
		} else {
			return valueOf(name);
		}
	}

	public String abbreviation() {
		return this.abbreviation;
	}
	
	static {
		ImmutableMap.Builder<String, ServerTickingPhase> builder = ImmutableMap.builder();
		for (ServerTickingPhase phase : values()) {
			builder.put(phase.abbreviation, phase);
		}
		
		PHASES_BY_ABBR = builder.build();
	}
}
