package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.data.SidebarDataSender;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TickingPhase;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class EntitySidebarCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entitysidebar")
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
												.executes(EntitySidebarCommand::addSidebar)
												.then(argument("whereToUpdate",StringArgumentType.string())
														.suggests((ct, b) -> {
															for(TickingPhase phase : TickingPhase.values()) {
																b.suggest(phase.name());
															}
															
															return b.buildFuture();
														})
														.executes(EntitySidebarCommand::addSidebarWithPoint)
														.then(argument("path", AccessingPathArgumentType.accessingPathArg())
																.executes((EntitySidebarCommand::addSidebarWithPointAndPath))))))))
				.then(literal("remove")
						.then(argument("name",StringArgumentType.string())
								.suggests((ct, b) -> {
									MessMod.INSTANCE.getServerHudManager().sidebar.getCustomLines().forEach((l) -> b.suggest(l.getName()));
									return b.buildFuture();
								})
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									SidebarDataSender sender = MessMod.INSTANCE.getServerHudManager().sidebar;
									if (sender.removeCustomLine(name)) {
										CommandUtil.feedbackWithArgs(ct, "Removed the line named %s from the sidebar.", name);
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.error(ct, "cmd.general.notfound");
										return 0;
									}
								})));
		dispatcher.register(command);
	}
	
	private static int addSidebar(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		Entity e = EntityArgumentType.getEntity(ct, "target");
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		SidebarDataSender sender = MessMod.INSTANCE.getServerHudManager().sidebar;
		try {
			if (sender.addLine(e, field, name, AccessingPath.DUMMY)) {
				CommandUtil.feedback(ct, "cmd.fixedentityhud.add");
			} else {
				CommandUtil.error(ct, "exp.dupname");
			}
			
			return Command.SINGLE_SUCCESS;
		} catch (TranslatableException e1) {
			CommandUtil.errorRaw(ct, e1.getMessage(), e1);
			return 0;
		}
	}
	
	private static int addSidebarWithPoint(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		Entity e = EntityArgumentType.getEntity(ct, "target");
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		SidebarDataSender sender = MessMod.INSTANCE.getServerHudManager().sidebar;
		try {
			if (sender.addLine(e, field, name, AccessingPath.DUMMY)) {
				CommandUtil.feedback(ct, "cmd.fixedentityhud.add");
			} else {
				CommandUtil.error(ct, "exp.dupname");
			}
			
			return Command.SINGLE_SUCCESS;
		} catch (TranslatableException e1) {
			CommandUtil.errorRaw(ct, e1.getMessage(), e1);
			return 0;
		}
	}
	
	private static int addSidebarWithPointAndPath(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		Entity e = EntityArgumentType.getEntity(ct, "target");
		String field = StringArgumentType.getString(ct, "field");
		String name = StringArgumentType.getString(ct, "name");
		AccessingPath path = AccessingPathArgumentType.getAccessingPath(ct, "path");
		SidebarDataSender sender = MessMod.INSTANCE.getServerHudManager().sidebar;
		try {
			if (sender.addLine(e, field, name, path)) {
				CommandUtil.feedback(ct, "cmd.fixedentityhud.add");
			} else {
				CommandUtil.error(ct, "exp.dupname");
			}
			
			return Command.SINGLE_SUCCESS;
		} catch (TranslatableException e1) {
			CommandUtil.errorRaw(ct, e1.getMessage(), e1);
			return 0;
		}
	}
}
