package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.CsvWriter;
import lovexyn0827.mess.mixins.NetworkStateAccessor;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.command.ServerCommandSource;

public class LogPacketCommand {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, Class<?>> SUBSCRIBED_TYPES = Maps.newConcurrentMap();
	private static final Map<String, Class<?>> PACKET_TYPES = Maps.newHashMap();
	private static volatile PacketRecorder packetRecorder = null;
	private static final Object2LongMap<Class<?>> STATS = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());
	
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
								})))
				.then(literal("startRecording")
						.then(argument("format", StringArgumentType.word())
								.suggests(CommandUtil.immutableSuggestions("csv"))
								.then(argument("saveTo", StringArgumentType.greedyString())
										.executes((ct) -> {
											if (packetRecorder != null) {
												CommandUtil.errorWithArgs(ct, "cmd.general.alstartlog");
												return 0;
											}
											
											File out = new File(StringArgumentType.getString(ct, "saveTo"));
											if (out.exists()) {
												CommandUtil.errorWithArgs(ct, "cmd.general.alexist", out);
												return 0;
											}

											PacketRecorder rec;
											String fmt = StringArgumentType.getString(ct, "format");
											switch (fmt) {
											case "csv":
												rec = new CsvRecorder();
												break;
											default:
												CommandUtil.errorWithArgs(ct, "cmd.general.nodef", fmt);
												return 0;
											}
											
											try {
												rec.start(out);
											} catch (IOException e) {
												LOGGER.error("Failed to start recording!", e);
												LOGGER.info("Recording file: {}", out.getAbsolutePath());
												CommandUtil.error(ct, "cmd.general.unexpected", e);
												return 0;
											}
											
											packetRecorder = rec;
											CommandUtil.feedback(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("stopRecording")
						.executes((ct) -> {
							PacketRecorder rec = packetRecorder;
							try {
								rec.close();
							} catch (Exception e) {
								LOGGER.error("Failed to stop recording!", e);
								CommandUtil.error(ct, "cmd.general.unexpected", e);
								return 0;
							}
							
							packetRecorder = null;
							CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("stats")
						.then(literal("reset")
								.executes((ct) -> {
									STATS.clear();
									CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								}))
						.executes((ct) -> {
							STATS.forEach((clz, count) -> {
								if (count != 0) {
									String className = MessMod.INSTANCE.getMapping().simpleNamedClass(clz);
									CommandUtil.feedbackRawWithArgs(ct, "%s: %d", className, count);
								}
							});
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("types", FilteredSetArgumentType.of(PACKET_TYPES.keySet(), (o) -> o))
								.executes((ct) -> {
									FilteredSetArgumentType.<String>getFiltered(ct, "types").forEach((className) -> {
										Class<?> pktClz = PACKET_TYPES.get(className);
										long count = STATS.getOrDefault(pktClz, 0);
										if (count != 0) {
											String shortClzName = MessMod.INSTANCE.getMapping()
													.simpleNamedClass(pktClz);
											CommandUtil.feedbackRawWithArgs(ct, "%s: %d", shortClzName, count);
										}
									});
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}

	public static boolean isSubscribed(Packet<?> packet) {
		return SUBSCRIBED_TYPES.values().contains(packet.getClass());
	}
	
	public static void onPacket(NetworkSide side, Packet<?> packet, boolean sending) {
		STATS.computeLong(packet.getClass(), (clz, original) -> (original == null ? 0 : original) + 1);
		String strRep = toString(packet);
		LOGGER.info(sending ? "{}: Sended Packet: {}" : "", 
				side == NetworkSide.CLIENTBOUND ? "CLIENT" : "SERVER", 
				toString(packet));
		if (packetRecorder != null) {
			try {
				packetRecorder.onPacket(side, packet, strRep, sending);
			} catch (IOException e) {
				LOGGER.error("Failed to record a packet!", e);
			}
		}
	}
	
	private static String toString(Packet<?> packet) {
		Mapping mapping = MessMod.INSTANCE.getMapping();
		StringBuilder sb = new StringBuilder(mapping.simpleNamedClass(packet.getClass().getName()));
		sb.append('[');
		for(Field f : packet.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				sb.append(mapping.namedField(f.getName())).append('=').append(f.get(packet)).append(',');
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		sb.append(']');
		return sb.toString();
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
		STATS.clear();
		if (packetRecorder != null) {
			try {
				packetRecorder.close();
			} catch (Exception e) {
				LOGGER.error("Failed to stop recording!", e);
			}
			
			packetRecorder = null;
		}
	}
	
	private interface PacketRecorder extends AutoCloseable, Flushable {
		void start(File file) throws IOException;
		void onPacket(NetworkSide side, Packet<?> packet, String strRep, boolean sending) throws IOException;
	}

	// TODO binary recording, zipped. e.g. rcvd/16703104212345.SomeC2SPacket
	private static class CsvRecorder implements PacketRecorder {
		private CsvWriter csv;
		
		@Override
		public void close() throws Exception {
			this.csv.close();
		}

		@Override
		public void flush() throws IOException {
			this.csv.flush();
		}

		@Override
		public void start(File file) throws IOException {
			// Not using BOS concerning thread safety.
			FileWriter recOut;
			recOut = new FileWriter(file);
			CsvWriter csv = new CsvWriter.Builder()
					.addColumn("Time")
					.addColumn("Nano")
					.addColumn("Side")
					.addColumn("Direction")
					.addColumn("Class")
					.addColumn("Detail")
					.build(recOut);
			this.csv = csv;
		}

		@Override
		public void onPacket(NetworkSide side, Packet<?> packet, String strRep, boolean sending) throws IOException {
			csv.println(
					System.currentTimeMillis(), 
					System.nanoTime(), 
					side == NetworkSide.CLIENTBOUND ? "Client" : "Server", 
					sending ? "Sending" : "Receiving", 
					MessMod.INSTANCE.getMapping().namedClass(packet.getClass().getCanonicalName()), 
					StringUtil.escapeCsv(strRep));
		}
	}
}
