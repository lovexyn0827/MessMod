package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
						}));
		dispatcher.register(command);
	}
}
