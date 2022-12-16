package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.access.CustomNode;
import net.minecraft.server.command.ServerCommandSource;

public class AccessingPathCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("accessingpath").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("defineNode")
						.then(argument("name", StringArgumentType.word())
								.then(argument("temporary", BoolArgumentType.bool())
										.then(argument("backend", StringArgumentType.greedyString())
												.executes((ct) -> {
													String name = StringArgumentType.getString(ct, "name");
													if(!CustomNode.NAME_PATTERN.matcher(name).matches()) {
														CommandUtil.error(ct, "cmd.accessingpath.invname");
														return 0;
													}
													
													String backendStr = StringArgumentType.getString(ct, "backend");
													try {
														CustomNode.define(name, backendStr, !BoolArgumentType.getBool(ct, "temporary")
																, ct.getSource().getMinecraftServer());
													} catch (TranslatableException e) {
														CommandUtil.errorRaw(ct, e.getMessage(), e);
														return 0;
													}

													CommandUtil.feedback(ct, "cmd.general.success");
													return Command.SINGLE_SUCCESS;
												})))))
				.then(literal("undefineNode")
						.then(argument("name", StringArgumentType.word())
								.suggests((ct, b) -> {
									CustomNode.listSuggestions(b);
									return b.buildFuture();
								})
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									try {
										CustomNode.undefine(name, ct.getSource().getMinecraftServer());
									} catch (TranslatableException e) {
										CommandUtil.errorRaw(ct, e.getMessage(), e);
										return 0;
									}

									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
