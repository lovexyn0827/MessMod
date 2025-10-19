package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class LogMovementCommand {
	public static final Set<EntityType<?>> AUTO_SUBSCRIBED_TYPES = Sets.newHashSet();
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logmovement").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									long count = l.stream().filter((e) -> !((EntityInterface) e).shouldLogMovement())
											.map((e) -> {
												((EntityInterface) e).setMovementSubscribed(true);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.general.sub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("target", EntityArgumentType.entities())
								.executes((ct) -> {
									Collection<? extends Entity> l = EntityArgumentType.getEntities(ct, "target");
									long count = l.stream().filter((e) -> ((EntityInterface) e).shouldLogMovement())
											.map((e) -> {
												((EntityInterface) e).setMovementSubscribed(false);
												return e;
											})
											.count();
									CommandUtil.feedbackWithArgs(ct, "cmd.general.unsub", l.size(), count);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("addAutoSubType")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									Identifier id = new Identifier(StringArgumentType.getString(ct, "entityType"));
									EntityType<?> type = Registries.ENTITY_TYPE.get(id);
									if (AUTO_SUBSCRIBED_TYPES.add(type)) {
										CommandUtil.feedbackWithArgs(ct, "cmd.general.autosub", id);
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.errorWithArgs(ct, "cmd.general.subdup", id);
										return 0;
									}
								})))
				.then(literal("stopAutoSubType")
						.then(argument("entityType", StringArgumentType.word())
								.suggests(CommandUtil.ENTITY_TYPES)
								.executes((ct) -> {
									Identifier id = new Identifier(StringArgumentType.getString(ct, "entityType"));
									EntityType<?> type = Registries.ENTITY_TYPE.get(id);
									AUTO_SUBSCRIBED_TYPES.remove(type);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.stopautosub", id);
									return 0;
								})));
		dispatcher.register(command);
	}
	
	public static void reset() {
		AUTO_SUBSCRIBED_TYPES.clear();
	}

	public static void tick(MinecraftServer server) {
		if(!AUTO_SUBSCRIBED_TYPES.isEmpty()) {
			server.getWorlds().forEach((world) -> {
				world.getEntitiesByType(null, (e) -> {
					return AUTO_SUBSCRIBED_TYPES.contains(e.getType());
				}).forEach((e) -> {
					((EntityInterface) e).setMovementSubscribed(true);
				});
			});
		}
	}
}
