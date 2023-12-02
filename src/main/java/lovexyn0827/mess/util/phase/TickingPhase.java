package lovexyn0827.mess.util.phase;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

public interface TickingPhase {
	public void triggerEvents(@Nullable World world);
	public void addEvent(Event event);
	public void removeEvent(Event event);
	public boolean isNotInAnyWorld();
	
	public static RequiredArgumentBuilder<ServerCommandSource, TickingPhase> commandArg() {
		return CommandManager.argument("whereToUpdate", TickingPhaseArgumentType.phaseArg());
	}

	public interface Event {
		void trigger(TickingPhase phase, @Nullable World world);
	}
}
