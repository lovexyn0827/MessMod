package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public class LogChunkBehaviorCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logchunkbehavior")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("start")
						.executes((ct) -> {
							if(MessMod.INSTANCE.getChunkLogger().isWorking()) {
								CommandUtil.error(ct, "cmd.logchunkbehavior.alreadystarted");
								return 0;
							}
							
							try {
								MessMod.INSTANCE.getChunkLogger().start();
							} catch (IOException e) {
								CommandUtil.error(ct, "cmd.general.unexpected");
								e.printStackTrace();
								return 0;
							}

							CommandUtil.feedback(ct, "cmd.general.success");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("stop")
						.executes((ct) -> {
							if(!MessMod.INSTANCE.getChunkLogger().isWorking()) {
								CommandUtil.error(ct, "cmd.logchunkbehavior.alreadystopped");
								return 0;
							}
							
							try {
								MessMod.INSTANCE.getChunkLogger().stop();
							} catch (IOException e) {
								CommandUtil.error(ct, "cmd.general.unexpected");
								e.printStackTrace();
								return 0;
							}
							
							CommandUtil.feedback(ct, "cmd.general.success");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("subscribe")
						.then(argument("events", EnumSetArgumentType.of(ChunkEvent.class))
								.executes((ct) -> {
									if(MessMod.INSTANCE.getChunkLogger().isWorking()) {
										CommandUtil.error(ct, "cmd.logchunkbehavior.reqidle");
										return 0;
									}
									
									Set<ChunkEvent> set = EnumSetArgumentType.<ChunkEvent>getEnums(ct, "events");
									MessMod.INSTANCE.getChunkLogger().subscribeAll(set);
									CommandUtil.feedbackWithArgs(ct,"cmd.general.submulti", set.size(), set);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsubscribe")
						.then(argument("events", EnumSetArgumentType.of(ChunkEvent.class))
								.executes((ct) -> {
									if(MessMod.INSTANCE.getChunkLogger().isWorking()) {
										CommandUtil.error(ct, "cmd.logchunkbehavior.reqidle");
										return 0;
									}
									
									Set<ChunkEvent> set = EnumSetArgumentType.<ChunkEvent>getEnums(ct, "events");
									MessMod.INSTANCE.getChunkLogger().unsubscribeAll(set);
									CommandUtil.feedbackWithArgs(ct,"cmd.general.unsubmulti", set.size(), set);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("listSubscribed")
						.executes((ct) -> {
							for(ChunkEvent event : MessMod.INSTANCE.getChunkLogger().listSubscribedEvents()) {
								CommandUtil.feedbackRaw(ct, event.name());
							}
							
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("addColumn")
						.then(argument("name", StringArgumentType.word())
								.then(argument("path", AccessingPathArgumentType.accessingPathArg(ServerWorld.class))
										.executes((ct) -> {
											if(addColumn(ct, 
													StringArgumentType.getString(ct, "name"), 
													AccessingPathArgumentType.getAccessingPath(ct, "path"))) {
												CommandUtil.feedback(ct, "cmd.general.success");
												return Command.SINGLE_SUCCESS;
											} else {
												return 0;
											}
										}))))
				.then(literal("removeColumn")
						.then(argument("name", StringArgumentType.word())
								.suggests((ct, builder) -> {
									MessMod.INSTANCE.getChunkLogger().getColumns().forEach(builder::suggest);
									return builder.buildFuture();
								})
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									if(MessMod.INSTANCE.getChunkLogger().removeColumn(name)) {
										CommandUtil.feedback(ct, "cmd.general.success");
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
										return 0;
									}
								})));
		dispatcher.register(command);
	}
	
	// Also responsible for sending error messages
	private static boolean addColumn(CommandContext<ServerCommandSource> ct, String name, AccessingPath path) {
		ChunkBehaviorLogger logger = MessMod.INSTANCE.getChunkLogger();
		if(logger.isWorking()) {
			CommandUtil.error(ct, "cmd.logchunkbehavior.reqidle");
			return false;
		} else {
			if(logger.getColumns().contains(name)) {
				CommandUtil.error(ct, "cmd.general.dupname");
				return false;
			} else {
				return logger.addColumn(name, path);
			}
		}
	}
}
