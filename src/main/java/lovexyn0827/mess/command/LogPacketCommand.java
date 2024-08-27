package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class LogPacketCommand {
	public static final TreeSet<Identifier> SUBSCRIBED_TYPES = Sets.newTreeSet();
	public static final TreeSet<Identifier> PACKET_TYPES = Sets.newTreeSet();
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logpacket").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("type", FilteredSetArgumentType.of(PACKET_TYPES, (o) -> o.getPath()))
								.executes((ct) -> {
									Set<Identifier> targets = FilteredSetArgumentType.getFiltered(ct, "type");
									if(targets.isEmpty()) {
										CommandUtil.error(ct, "cmd.general.nomatching");
										return 0;
									}
									
									targets.forEach((id) -> SUBSCRIBED_TYPES.add(id));
									CommandUtil.feedbackWithArgs(ct, "cmd.general.submulti", targets.size(), targets);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("type", FilteredSetArgumentType.of(PACKET_TYPES, (o) -> o.getPath()))
								.executes((ct) -> {
									Set<Identifier> targets = FilteredSetArgumentType.getFiltered(ct, "type");
									if(targets.isEmpty()) {
										CommandUtil.error(ct, "cmd.general.nomatching");
										return 0;
									}
									
									targets.forEach(SUBSCRIBED_TYPES::remove);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.unsubmulti", targets.size(), targets);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}

	public static boolean isSubscribed(Packet<?> packet) {
		return SUBSCRIBED_TYPES.contains(packet.getPacketId().id());
	}
	
	public static void reset() {
		SUBSCRIBED_TYPES.clear();
	}
}
