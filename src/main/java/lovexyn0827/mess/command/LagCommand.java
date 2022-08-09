package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;

public class LagCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("lag").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("nanoseconds", LongArgumentType.longArg())
						.executes(LagCommand::lagServer)
						.then(argument("thread", StringArgumentType.word())
								.suggests((ct, b) -> {
									for(MainThread t : MainThread.values()) {
										if(t.available()) {
											b.suggest(t.name());
										}
									}
									
									return b.buildFuture();
								})
								.executes(LagCommand::lag)));
		dispatcher.register(command);
	}
	
	private static int lagServer(CommandContext<ServerCommandSource> ct) {
		long nanos = LongArgumentType.getLong(ct, "nanoseconds");
		try {
			Thread.sleep((long) (nanos / 1000000), (int) (nanos % 1000000));
		} catch (InterruptedException e) {
			e.printStackTrace();
			CommandUtil.error(ct, "cmd.general.unexpected", e);
		}
		
		CommandUtil.feedback(ct, "cmd.lag.done");
		return Command.SINGLE_SUCCESS;
	}

	private static int lag(CommandContext<ServerCommandSource> ct) {
		long nanos = LongArgumentType.getLong(ct, "nanoseconds");
		try {
			MainThread thread = MainThread.valueOf(StringArgumentType.getString(ct, "thread"));
			if(!thread.available()) {
				CommandUtil.error(ct, "cmd.lag.unsupported");
			}
			
			MutableInt result = new MutableInt(Command.SINGLE_SUCCESS);
			Runnable lagAsync = () -> {
				try {
					Thread.sleep((long) (nanos / 1000000), (int) (nanos % 1000000));
					ct.getSource().getServer().execute(() -> {
						CommandUtil.feedback(ct, "cmd.lag.done");
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
					ct.getSource().getServer().execute(() -> {
						CommandUtil.feedback(ct, "cmd.general.unexpected");
					});
					result.setValue(0);
				}
			};
			switch (thread) {
			case CLIENT:
				MinecraftClient.getInstance().execute(lagAsync);
				break;
//			case CLIENT_NETWORK:
//				break;
			case SERVER:
				return lagServer(ct);
//			case SERVER_NETWORK:
//				break;
			}
			
			return result.getValue();
		} catch (IllegalArgumentException e) {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", StringArgumentType.getString(ct, "thread"));
			e.printStackTrace();
			return 0;
		}
	}

	private static enum MainThread {
		SERVER(true), 
		CLIENT(false);
//		SERVER_NETWORK(true), 
//		CLIENT_NETWORK(false);
		
		private final boolean supportsDedicatedEnv;

		private MainThread(boolean supportsDedicatedEnv) {
			this.supportsDedicatedEnv = supportsDedicatedEnv;
		}
		
		public boolean available() {
			return this.supportsDedicatedEnv || !MessMod.isDedicatedEnv();
		}
	}
}
