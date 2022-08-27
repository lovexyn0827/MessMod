package lovexyn0827.mess.util;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

// TODO Event system
public enum TickingPhase {
	WEATHER_CYCLE(false),
	CHUNK(false),
	SCHEDULED_TICK(false),
	VILLAGE(false),
	BLOCK_EVENT(false),
	ENTITY(false),
	TILE_ENTITY(false),
	TICKED_ALL_WORLDS(true), 
	SERVER_TASKS(true);
	
	private final List<Event> events = Lists.newArrayList();
	public final boolean notInAnyWorld;
	
	private TickingPhase(boolean notInAnyWorld) {
		this.notInAnyWorld = notInAnyWorld;
	}
	
	public void triggerEvents(@Nullable ServerWorld world) {
		this.events.forEach((e) -> e.trigger(this, world));
	}

	public void addEvent(Event event) {
		this.events.add(event);
	}

	public void removeEvent(Event event) {
		this.events.add(event);
	}

	public static void addEventToAll(Event event) {
		for(TickingPhase phase : values()) {
			phase.addEvent(event);
		}
	}
	
	public static void removeAllEvents() {
		for(TickingPhase phase : values()) {
			phase.events.clear();
		}
	}

	public static RequiredArgumentBuilder<ServerCommandSource, String> commandArg() {
		return CommandManager.argument("whereToUpdate",StringArgumentType.string())
		.suggests((ct, b) -> {
			for(TickingPhase phase : values()) {
				b.suggest(phase.name());
			}
			
			return b.buildFuture();
		});
	}

	public interface Event {
		void trigger(TickingPhase phase, @Nullable ServerWorld world);
	}
}
