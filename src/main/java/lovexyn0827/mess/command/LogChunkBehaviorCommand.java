package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import net.minecraft.server.command.ServerCommandSource;

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
				.then(literal("setSubscribed")
						.then(argument("event", StringArgumentType.word())
								.suggests(CommandUtil.immutableSuggestionsOfEnum(ChunkEvent.class))
								.then(argument("enabled", BoolArgumentType.bool())
										.executes((ct) -> {
											if(MessMod.INSTANCE.getChunkLogger().isWorking()) {
												CommandUtil.error(ct, "cmd.logchunkbehavior.reqidle");
												return 0;
											}
											
											String eventName = StringArgumentType.getString(ct, "event");
											ChunkEvent event;
											try {
												event = ChunkEvent.valueOf(eventName);
											} catch (IllegalArgumentException e) {
												CommandUtil.errorWithArgs(ct, "%s was not defined", eventName);
												return 0;
											}
											
											boolean enabled = BoolArgumentType.getBool(ct, "enabled");
											MessMod.INSTANCE.getChunkLogger().setSubscribed(event, enabled);
											CommandUtil.feedbackWithArgs(ct, 
													enabled ? "cmd.general.subgen" : "cmd.general.unsubgen", eventName);
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("listSubscribed")
						.executes((ct) -> {
							for(ChunkEvent event : MessMod.INSTANCE.getChunkLogger().listSubscribedEvents()) {
								CommandUtil.feedbackRaw(ct, event.name());
							}
							
							return Command.SINGLE_SUCCESS;
						}));
		dispatcher.register(command);
	}
}
