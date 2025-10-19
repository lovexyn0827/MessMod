package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

// Maybe i can name it /setblockplusplus?
public class SetBlockRawCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("setblockraw").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("pos", BlockPosArgumentType.blockPos())
						.then(argument("block", BlockStateArgumentType.blockState())
								.executes((ct) -> {
									BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
									ServerWorld world = ct.getSource().getWorld();
									BlockState block = BlockStateArgumentType
											.getBlockState(ct, "block")
											.getBlockState();
									world.setBlockState(pos, block);
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("flags", IntegerArgumentType.integer())
										.executes((ct) -> {
											BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
											ServerWorld world = ct.getSource().getWorld();
											BlockState block = BlockStateArgumentType
													.getBlockState(ct, "block")
													.getBlockState();
											int flags = IntegerArgumentType.getInteger(ct, "flags");
											world.setBlockState(pos, block, flags);
											CommandUtil.feedback(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("depth", IntegerArgumentType.integer())
												.executes((ct) -> {
													BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
													ServerWorld world = ct.getSource().getWorld();
													BlockState block = BlockStateArgumentType
															.getBlockState(ct, "block")
															.getBlockState();
													int flags = IntegerArgumentType.getInteger(ct, "flags");
													int depth = IntegerArgumentType.getInteger(ct, "depth");
													world.setBlockState(pos, block, flags, depth);
													CommandUtil.feedback(ct, "cmd.general.success");
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("p2", BlockPosArgumentType.blockPos())
														.executes((ct) -> {
															BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
															BlockPos p2 = BlockPosArgumentType.getBlockPos(ct, "p2");
															ServerWorld world = ct.getSource().getWorld();
															BlockState block = BlockStateArgumentType
																	.getBlockState(ct, "block")
																	.getBlockState();
															int flags = IntegerArgumentType.getInteger(ct, "flags");
															int depth = IntegerArgumentType.getInteger(ct, "depth");
															for (BlockPos cur : BlockPos.iterate(pos, p2)) {
																world.setBlockState(cur, block, flags, depth);
															}
															
															CommandUtil.feedback(ct, "cmd.general.success");
															return Command.SINGLE_SUCCESS;
														}))))));
		dispatcher.register(command);
	}
}
