package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.electronic.WaveForm;
import lovexyn0827.mess.electronic.WaveGenerator;
import lovexyn0827.mess.util.TranslatableException;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class WaveGenCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("wavegen").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("new")
						.then(argument("pos", BlockPosArgumentType.blockPos())
								.then(argument("wavedef", StringArgumentType.greedyString())
										.executes((ct) -> {
											BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
											if (ct.getSource().getWorld().getBlockState(pos).getBlock() != Blocks.LOOM) {
												CommandUtil.error(ct, "cmd.wavegen.reqloom");
												return 0;
											}

											WaveForm wave;
											try {
												wave = WaveForm.parse(new StringReader(StringArgumentType.getString(ct, "wavedef")));
											} catch (TranslatableException e) {
												CommandUtil.error(ct, e.getLocalizedMessage());
												return 0;
											}
											
											WaveGenerator.remove(ct.getSource().getWorld().getRegistryKey(), pos);
											wave.register(ct.getSource().getWorld(), pos);
											CommandUtil.feedback(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("remove")
						.then(argument("pos", BlockPosArgumentType.blockPos())
								.suggests(WaveGenerator::suggestDefinedPos)
								.executes((ct) -> {
									BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
									WaveGenerator.remove(ct.getSource().getWorld().getRegistryKey(), pos);
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
	
	static void reset() {
		WaveGenerator.reset();
	}

}
