package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.NetworkStateAccessor;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.command.ServerCommandSource;

public class LogPacketCommand {
	public static final Set<Class<?>> SUBSCRIBED_TYPES = Sets.newHashSet();
	public static final Map<String, Class<?>> PACKET_TYPES = Maps.newHashMap();
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("logpacket").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("sub")
						.then(argument("type", StringArgumentType.word())
								.suggests((ct, b) -> {
									PACKET_TYPES.keySet().forEach(b::suggest);
									return b.buildFuture();
								})
								.executes((ct) -> {
									Class<?> type = PACKET_TYPES.get(StringArgumentType.getString(ct, "type"));
									if(type != null) {
										SUBSCRIBED_TYPES.add(type);
										CommandUtil.feedback(ct, "cmd.general.success");
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", 
												StringArgumentType.getString(ct, "type"));
										return 0;
									}
								})))
				.then(literal("unsub")
						.then(argument("type", StringArgumentType.word())
								.suggests((ct, b) -> {
									SUBSCRIBED_TYPES.forEach((c) -> b.suggest(c.getSimpleName()));
									return b.buildFuture();
								})
								.executes((ct) -> {
									Class<?> type = PACKET_TYPES.get(StringArgumentType.getString(ct, "type"));
									if(type != null) {
										SUBSCRIBED_TYPES.remove(type);
										CommandUtil.feedback(ct, "cmd.general.success");
										return Command.SINGLE_SUCCESS;
									} else {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", 
												StringArgumentType.getString(ct, "type"));
										return 0;
									}
								})));
		dispatcher.register(command);
	}

	public static boolean isSubscribed(Packet<?> packet) {
		for(Class<?> clazz : SUBSCRIBED_TYPES) {
			if(clazz.isInstance(packet)) {
				return true;
			}
		}
		
		return false;
	}
	
	static {
		try {
			Mapping mapping = MessMod.INSTANCE.getMapping();
			for(NetworkState state : NetworkState.values()) {
				Map<NetworkSide, ? extends PacketHandler<?>> handlerMap = 
						((NetworkStateAccessor)(Object) state).getHandlerMap();
				handlerMap.values().forEach((handler) -> {
					try {
						Iterable<Class<? extends Packet<?>>> classes = handler.getPacketTypes();
						for(Class<?> clazz : classes) {
							PACKET_TYPES.put(mapping.simpleNamedClass(clazz.getName()), clazz);
						}
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
