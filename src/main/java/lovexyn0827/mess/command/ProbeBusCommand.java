package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import lovexyn0827.mess.mixins.WorldSavePathMixin;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class ProbeBusCommand {
	private static final WorldSavePath BUS_DEFINITION_FILE = WorldSavePathMixin.create("bus_definition.prop");
	private static final Map<String, Bus> BUSES = new HashMap<>();
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("probebus")
				.requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("probe")
						.then(argument("bit0", BlockPosArgumentType.blockPos())
								.then(argument("spacing", BlockPosArgumentType.blockPos())
										.then(argument("bits", IntegerArgumentType.integer(1))
												.executes((ct) -> {
													BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
													BlockPos spacing = BlockPosArgumentType.getBlockPos(ct, "spacing");
													int bits = IntegerArgumentType.getInteger(ct, "bits");
													return executeShowValue(ct, createBus(ct.getSource().getWorld(), bit0, spacing, bits), "bin");
												})
												.then(argument("radix", StringArgumentType.word())
														.suggests(CommandUtil.immutableSuggestions("bin", "dec", "hex"))
														.executes((ct) -> {
															BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
															BlockPos spacing = BlockPosArgumentType.getBlockPos(ct, "spacing");
															int bits = IntegerArgumentType.getInteger(ct, "bits");
															String radix = StringArgumentType.getString(ct, "radix");
															return executeShowValue(ct, createBus(ct.getSource().getWorld(), bit0, spacing, bits), radix);
														}))))))
				.then(literal("newBus")
						.then(argument("name", StringArgumentType.word())
								.then(argument("bit0", BlockPosArgumentType.blockPos())
										.executes((ct) -> {
											String name = StringArgumentType.getString(ct, "name");
											BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
											return registerBus(ct, name, bit0, BlockPos.ORIGIN, 1, false);
										})
										.then(literal("permanent")
												.executes((ct) -> {
													String name = StringArgumentType.getString(ct, "name");
													BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
													return registerBus(ct, name, bit0, BlockPos.ORIGIN, 1, true);
												}))
										.then(argument("spacing", BlockPosArgumentType.blockPos())
												.then(argument("bits", IntegerArgumentType.integer(1))
														.executes((ct) -> {
															String name = StringArgumentType.getString(ct, "name");
															BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
															BlockPos spacing = BlockPosArgumentType.getBlockPos(ct, "spacing");
															int bits = IntegerArgumentType.getInteger(ct, "bits");
															return registerBus(ct, name, bit0, spacing, bits, false);
														})
														.then(literal("permanent")
																.executes((ct) -> {
															String name = StringArgumentType.getString(ct, "name");
															BlockPos bit0 = BlockPosArgumentType.getBlockPos(ct, "bit0");
															BlockPos spacing = BlockPosArgumentType.getBlockPos(ct, "spacing");
															int bits = IntegerArgumentType.getInteger(ct, "bits");
															return registerBus(ct, name, bit0, spacing, bits, true);
														})))))))
				.then(literal("removeBus")
						.then(argument("name", StringArgumentType.word())
								.suggests((ct, b) -> {
									BUSES.keySet().forEach(b::suggest);
									return b.buildFuture();
								})
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									Bus bus = BUSES.remove(name);
									if (bus == null) {
										CommandUtil.errorWithArgs(ct, "cmd.general.undef", name);
										return 0;
									}
									
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})))
				.then(literal("probeSaved")
						.then(argument("name", StringArgumentType.word())
								.suggests((ct, b) -> {
									BUSES.keySet().forEach(b::suggest);
									return b.buildFuture();
								})
								.executes((ct) -> {
									String name = StringArgumentType.getString(ct, "name");
									Bus bus = BUSES.get(name);
									if (bus == null) {
										CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
										return 0;
									}
									
									return executeShowValue(ct, bus, "bin");
								})
								.then(argument("radix", StringArgumentType.word())
										.suggests(CommandUtil.immutableSuggestions("bin", "dec", "hex"))
										.executes((ct) -> {
											String name = StringArgumentType.getString(ct, "name");
											Bus bus = BUSES.get(name);
											String radix = StringArgumentType.getString(ct, "radix");
											return executeShowValue(ct, bus, radix);
										}))));
		dispatcher.register(command);
	}
	
	private static int registerBus(CommandContext<ServerCommandSource> ct, 
			String name, BlockPos bit0, BlockPos spacing, int bits, boolean permanent) {
		ServerWorld world = ct.getSource().getWorld();
		Bus bus = createBus(world, bit0, spacing, bits);
		BUSES.put(name, bus);
		if (!permanent) {
			CommandUtil.feedback(ct, "cmd.general.success");
			return Command.SINGLE_SUCCESS;
		}
		
		Path file = world.getServer().getSavePath(BUS_DEFINITION_FILE);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
			Properties prop = readBusDefinitions(world.getServer());
			prop.put(name, serializeBus(world, bit0, spacing, bits));
			prop.store(writer, "MessMod bus definitions for /probebus");
			CommandUtil.feedback(ct, "cmd.general.success");
			return Command.SINGLE_SUCCESS;
		} catch (IOException e) {
			CommandUtil.errorWithArgs(ct, e, "cmd.probebus.savefail", e.getMessage());
			return Command.SINGLE_SUCCESS;
		}
	}

	private static Properties readBusDefinitions(MinecraftServer server) {
		Path file = server.getSavePath(BUS_DEFINITION_FILE);
		Properties prop = new Properties();
		try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
			prop.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return prop;
	}

	@Nullable
	private static Bus deserializeBus(MinecraftServer server, String busDef) {
		try (Scanner s = new Scanner(new StringReader(busDef))) {
			String worldKeyId = s.next();
			RegistryKey<World> worldKey = RegistryKey.of(Registry.DIMENSION, new Identifier(worldKeyId));
			ServerWorld world = server.getWorld(worldKey);
			BlockPos bit0 = new BlockPos(s.nextInt(), s.nextInt(), s.nextInt());
			BlockPos spacing = new BlockPos(s.nextInt(), s.nextInt(), s.nextInt());
			int bits = s.nextInt();
			return createBus(world, bit0, spacing, bits);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String serializeBus(ServerWorld world, BlockPos bit0, BlockPos spacing, int bits) {
		return String.format("%s %d %d %d %d %d %d %d", 
				world.getRegistryKey().getValue().toString(), 
				bit0.getX(), bit0.getY(), bit0.getZ(), 
				spacing.getX(), spacing.getY(), spacing.getZ(), 
				bits);
	}

	private static int executeShowValue(CommandContext<ServerCommandSource> ct, Bus bus, String radix) {
		String value;
		switch (radix) {
		case "bin":
			value = bus.readBinary();
			break;
		case "dec":
			value = bus.readDecimal();
			break;
		case "hex":
			value = bus.readHexadecimal();
			break;
		default:
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", radix);
			return 0;
		}
		
		CommandUtil.feedback(ct, value);
		return Command.SINGLE_SUCCESS;
	}
	
	private static Bus createBus(ServerWorld world, BlockPos bit0, BlockPos spacing, int bits) {
		Bus bus = new Bus(world);
		BlockPos probe = bit0;
		for (int i = 0; i < bits; i++) {
			bus.addProbe(probe);
			probe = probe.add(spacing);
		}
		
		return bus;
	}
	
	public static void reset() {
		BUSES.clear();
	}
	
	public static void reload(MinecraftServer server) {
		Properties prop = readBusDefinitions(server);
		prop.forEach((nameObj, busDefObj) -> {
			String name = (String) nameObj;
			String busDef = (String) busDefObj;
			BUSES.put(name, deserializeBus(server, busDef));
		});
	}
	
	private static final class Bus {
		private final ServerWorld world;
		private final List<BlockPos> probes = new ArrayList<>();
		
		private Bus(ServerWorld world) {
			this.world = world;
		}
		
		private void addProbe(BlockPos pos) {
			this.probes.add(pos);
		}
		
		private BitSet probe() {
			BitSet busData = new BitSet(this.probes.size());
			for (int i = 0; i < this.probes.size(); i++) {
				if (this.world.getReceivedRedstonePower(this.probes.get(i)) > 0) {
					busData.set(i);
				}
			}
			
			return busData;
		}
		
		private String readBinary() {
			BitSet busData = this.probe();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.probes.size(); i++) {
				if (i % 4 == 0 && i != 0) {
					sb.insert(0, ' ');
				}
				
				sb.insert(0, busData.get(i) ? '1' : '0');
			}
			
			return sb.toString();
		}
		
		private String readHexadecimal() {
			BitSet busData = this.probe();
			StringBuilder sb = new StringBuilder();
			int hexDigit = 0;
			for (int i = 0; i < this.probes.size(); i++) {
				if (i % 4 == 0 && i != 0) {
					sb.insert(0, Integer.toHexString(hexDigit));
					hexDigit = 0;
				}
				
				if (busData.get(i)) {
					hexDigit |= 1 << (i % 4);
				}
			}
			
			sb.insert(0, Integer.toHexString(hexDigit));
			return sb.toString();
		}
		
		private String readDecimal() {
			BitSet busData = this.probe();
			BigDecimal num = new BigDecimal(0);
			BigDecimal two = new BigDecimal(2);
			for (int i = this.probes.size() - 1; i >= 0; i--) {
				num = num.multiply(two);
				if (busData.get(i)) {
					num = num.add(BigDecimal.ONE);
				}
			}
			
			return num.toPlainString();
		}
	}
}
