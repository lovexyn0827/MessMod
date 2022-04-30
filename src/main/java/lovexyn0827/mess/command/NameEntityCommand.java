package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class NameEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("namentity").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("entities", EntityArgumentType.entities())
						.then(argument("name", StringArgumentType.greedyString())
								.executes((ct) -> {
									EntityArgumentType.getEntities(ct, "entities").forEach((e) -> {
										e.setCustomName(new LiteralText(StringArgumentType.getString(ct, "name")));
										e.setCustomNameVisible(true);
									});
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
