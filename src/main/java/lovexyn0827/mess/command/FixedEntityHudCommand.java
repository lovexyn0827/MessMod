package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class FixedEntityHudCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("fixedentityhud")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("add")
						.then(argument("target",EntityArgumentType.entity())
								.then(argument("field",StringArgumentType.string())
										.suggests((ct,builder)->{
											for(String fieldName : Reflection.getAvailableFields(EntityArgumentType.getEntity(ct, "target").getClass())) {
												builder = builder.suggest(fieldName);
											}
											
											builder.suggest("-THIS-");
											return builder.buildFuture();
										})
										.then(argument("name",StringArgumentType.string())
												.then(argument("whereToUpdate",StringArgumentType.string())
														.suggests((ct, b) -> {
															return b.buildFuture();
														})
														.executes(FixedEntityHudCommand::addSidebar)
														.then(argument("path", AccessingPathArgumentType.accessingPathArg())
																.executes((ct) -> {
																	return Command.SINGLE_SUCCESS;
																})))))));
		dispatcher.register(command);
	}
	
	private static int addSidebar(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		Entity e = EntityArgumentType.getEntity(ct, "target");
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addSidebarWithPath(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		Entity e = EntityArgumentType.getEntity(ct, "target");
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		return Command.SINGLE_SUCCESS;
	}
}
