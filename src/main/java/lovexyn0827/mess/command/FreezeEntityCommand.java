package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

public class FreezeEntityCommand {
	public static final Set<Entity> FROZEN_ENTITIES = Sets.newHashSet();
	
	public static void reset() {
		FROZEN_ENTITIES.clear();
	}
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("freezentity").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("freeze")
						.then(argument("entities", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "entities");
									long count = l.stream().filter((e) -> !FROZEN_ENTITIES.contains(e) && !(e instanceof PlayerEntity))
											.map(FROZEN_ENTITIES::add).count();
									CommandUtil.feedbackWithArgs(ct, "cmd.freezentity.sub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("resume")
						.then(argument("entities", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "entities");
									long count = l.stream().filter((e) -> FROZEN_ENTITIES.contains(e))
											.map(FROZEN_ENTITIES::remove).count();
									CommandUtil.feedbackWithArgs(ct, "cmd.freezentity.unsub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})));;
		dispatcher.register(command);
	}
}
