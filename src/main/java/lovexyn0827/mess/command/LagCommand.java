package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.phase.TickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase.Event;
import lovexyn0827.mess.util.phase.TickingPhaseArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;

public class LagCommand {
	private final static Set<LaggingEvent> ONGOING_LAGGING_EVENTS = new HashSet<>();
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("lag").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("once")
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
										.executes(LagCommand::lag))))
				.then(literal("while")
						.then(argument("nanoseconds", LongArgumentType.longArg())
								.then(argument("ticks", IntegerArgumentType.integer(0))
										.then(argument("phase", TickingPhaseArgumentType.phaseArg())
												.executes(LagCommand::lagForAWhile)))));
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
	
	private static int lagForAWhile(CommandContext<ServerCommandSource> ct) {
		long nanos = LongArgumentType.getLong(ct, "nanoseconds");
		int ticks = IntegerArgumentType.getInteger(ct, "ticks");
		TickingPhase phase = TickingPhaseArgumentType.getPhase(ct, "phase");
		Event event = (p, w) -> {
			try {
				Thread.sleep((long) (nanos / 1000000), (int) (nanos % 1000000));
			} catch (InterruptedException e) {
				e.printStackTrace();
				CommandUtil.error(ct, "cmd.general.unexpected", e);
			}
		};
		ONGOING_LAGGING_EVENTS.add(new LaggingEvent(event, phase, 
				ct.getSource().getServer().getOverworld().getTime() + ticks));
		phase.addEvent(event);
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	public static void tick() {
		Iterator<LaggingEvent> itr = ONGOING_LAGGING_EVENTS.iterator();
		while(itr.hasNext()) {
			LaggingEvent e = itr.next();
			if(MessMod.INSTANCE.getGameTime() >= e.expiration) {
				itr.remove();
				e.phase.removeEvent(e.event);
			}
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
	
	private static final class LaggingEvent {
		protected final TickingPhase.Event event;
		protected final TickingPhase phase;
		protected final long expiration;
		
		protected LaggingEvent(Event event, TickingPhase phase, long expiration) {
			this.event = event;
			this.phase = phase;
			this.expiration = expiration;
		}
	}
}
