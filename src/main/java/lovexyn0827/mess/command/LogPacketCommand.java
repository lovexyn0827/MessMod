package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.NetworkStateAccessor;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.command.ServerCommandSource;

public class LogPacketCommand {
	public static final Map<String, Class<?>> SUBSCRIBED_TYPES = Maps.newHashMap();
	public static final Map<String, Class<?>> PACKET_TYPES = Maps.newHashMap();
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logpacket").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("type", FilteredSetArgumentType.of(PACKET_TYPES.keySet(), (o) -> o))
								.executes((ct) -> {
									Set<String> classes = FilteredSetArgumentType.<String>getFiltered(ct, "type");
									if(classes.isEmpty()) {
										CommandUtil.error(ct, "cmd.general.nomatching");
										return 0;
									}
									
									classes.forEach((cn) -> SUBSCRIBED_TYPES.put(cn, PACKET_TYPES.get(cn)));
									CommandUtil.feedbackWithArgs(ct, "cmd.general.submulti", classes.size(), classes);
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("unsub")
						.then(argument("type", FilteredSetArgumentType.of(PACKET_TYPES.keySet(), (o) -> o))
								.executes((ct) -> {
									Set<String> classes = FilteredSetArgumentType.<String>getFiltered(ct, "type");
									if(classes.isEmpty()) {
										CommandUtil.error(ct, "cmd.general.nomatching");
										return 0;
									}
									
									classes.forEach(SUBSCRIBED_TYPES::remove);
									CommandUtil.feedbackWithArgs(ct, "cmd.general.unsubmulti", classes.size(), classes);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}

	public static boolean isSubscribed(Packet<?> packet) {
		return SUBSCRIBED_TYPES.values().contains(packet.getClass());
	}
	
	static {
		try {
			Mapping mapping = MessMod.INSTANCE.getMapping();
			for(NetworkState state : NetworkState.values()) {
				Map<NetworkSide, ? extends PacketHandler<?>> handlerMap = 
						((NetworkStateAccessor)(Object) state).getHandlerMap();
				handlerMap.values().forEach((handler) -> {
					try {
						handler.forEachPacketType((clazz) -> {
							PACKET_TYPES.put(mapping.simpleNamedClass(clazz.getName()), clazz);
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void reset() {
		SUBSCRIBED_TYPES.clear();
	}
}
