package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class StackEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("stackentity").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("entity", EntityArgumentType.entities())
						.then(argument("count", IntegerArgumentType.integer(1))
								.executes((ct) -> {
									Entity center = EntityArgumentType.getEntity(ct, "entity");
									int count = IntegerArgumentType.getInteger(ct, "count");
									// Summon (n - 1) entities, so that the resulting stack contains n entities.
									for (int i = 1; i < count; i++) {
										Entity e = center.getType().create(center.getWorld());
										e.copyPositionAndRotation(center);
										e.setVelocity(center.getVelocity());
									}
									
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
