package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.function.BiConsumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TouchCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("touch").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("from", BlockPosArgumentType.blockPos())
						.executes((ct) -> {
							BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "from");
							ServerWorld world = ct.getSource().getWorld();
							performFullUpdates(pos, world);
							CommandUtil.feedback(ct, "cmd.general.success");
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("to", BlockPosArgumentType.blockPos())
								.executes((ct) -> {
									forEachPos(ct, TouchCommand::performFullUpdates);
									CommandUtil.feedback(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})
								.then(literal("nc")
										.executes((ct) -> {
											forEachPos(ct, TouchCommand::ncUpdateInAllDirections);
											CommandUtil.feedback(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("srcPos", BlockPosArgumentType.blockPos())
												.executes((ct) -> {
													BlockPos srcPos = BlockPosArgumentType.getBlockPos(ct, "srcPos");
													forEachPos(ct, (pos, world) -> {
														BlockState srcBlock = world.getBlockState(srcPos);
														world.updateNeighbor(srcPos, srcBlock.getBlock(), pos);
													});
													CommandUtil.feedback(ct, "cmd.general.success");
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("srcBlock", BlockStateArgumentType.blockState())
														.executes((ct) -> {
															BlockPos srcPos = BlockPosArgumentType.getBlockPos(ct, "srcPos");
															Block srcBlock = BlockStateArgumentType
																	.getBlockState(ct, "srcBlock")
																	.getBlockState()
																	.getBlock();
															forEachPos(ct, (pos, world) -> {
																world.updateNeighbor(srcPos, srcBlock, pos);
															});
															CommandUtil.feedback(ct, "cmd.general.success");
															return Command.SINGLE_SUCCESS;
														}))))
								.then(literal("pp")
										.executes((ct) -> {
											forEachPos(ct, (pos, world) -> {
												BlockState state = world.getBlockState(pos);
												ppUpdate(world, pos, state, state, 3, 512);
											});
											CommandUtil.feedback(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										})
										.then(argument("maxUpdateDepth", IntegerArgumentType.integer())
												.executes((ct) -> {
													int maxUpdateDepth = IntegerArgumentType.getInteger(ct, "maxUpdateDepth");
													forEachPos(ct, (pos, world) -> {
														BlockState state = world.getBlockState(pos);
														ppUpdate(world, pos, state, state, 3, maxUpdateDepth);
													});
													CommandUtil.feedback(ct, "cmd.general.success");
													return Command.SINGLE_SUCCESS;
												})
												.then(argument("flags", IntegerArgumentType.integer())
														.executes((ct) -> {
															int maxUpdateDepth = IntegerArgumentType.getInteger(ct, "maxUpdateDepth");
															int flags = IntegerArgumentType.getInteger(ct, "flags");
															forEachPos(ct, (pos, world) -> {
																BlockState state = world.getBlockState(pos);
																ppUpdate(world, pos, state, state, flags, maxUpdateDepth);
															});
															CommandUtil.feedback(ct, "cmd.general.success");
															return Command.SINGLE_SUCCESS;
														})
														.then(argument("oldState", BlockStateArgumentType.blockState())
																.executes((ct)-> {
																	int maxUpdateDepth = IntegerArgumentType.getInteger(ct, "maxUpdateDepth");
																	int flags = IntegerArgumentType.getInteger(ct, "flags");
																	BlockState oldState = BlockStateArgumentType
																			.getBlockState(ct, "oldState")
																			.getBlockState();
																	forEachPos(ct, (pos, world) -> {
																		BlockState state = world.getBlockState(pos);
																		ppUpdate(world, pos, oldState, state, flags, maxUpdateDepth);
																	});
																	CommandUtil.feedback(ct, "cmd.general.success");
																	return Command.SINGLE_SUCCESS;
																})
																.then(argument("newState", BlockStateArgumentType.blockState())
																		.executes((ct) -> {
																			int maxUpdateDepth = IntegerArgumentType.getInteger(ct, "maxUpdateDepth");
																			int flags = IntegerArgumentType.getInteger(ct, "flags");
																			BlockState oldState = BlockStateArgumentType
																					.getBlockState(ct, "oldState")
																					.getBlockState();
																			BlockState newState = BlockStateArgumentType
																					.getBlockState(ct, "oldState")
																					.getBlockState();
																			forEachPos(ct, (pos, world) -> {
																				ppUpdate(world, pos, oldState, newState, flags, maxUpdateDepth);
																			});
																			CommandUtil.feedback(ct, "cmd.general.success");
																			return Command.SINGLE_SUCCESS;
																		}))))))));
		dispatcher.register(command);
	}

	private static void performFullUpdates(BlockPos pos, ServerWorld world) {
		ncUpdateInAllDirections(pos, world);
		for (Direction dir : Direction.values()) {
			BlockPos srcPos = pos.offset(dir);
			BlockState srcBlock = world.getBlockState(srcPos);
			ppUpdate(world, srcPos, srcBlock, srcBlock, 3, 512);
		}
	}

	private static void ncUpdateInAllDirections(BlockPos pos, ServerWorld world) {
		for (Direction dir : Direction.values()) {
			BlockPos srcPos = pos.offset(dir);
			BlockState srcBlock = world.getBlockState(srcPos);
			world.updateNeighbor(srcPos, srcBlock.getBlock(), pos);
		}
		
		BlockState srcBlock = world.getBlockState(pos);
		world.updateNeighbor(pos, srcBlock.getBlock(), pos);
	}
	
	private static void ppUpdate(ServerWorld world, BlockPos pos, BlockState oldState, BlockState newState, 
			int flags, int maxUpdateDepth) {
		int i = flags & 0xFFFFFFDE;
		oldState.prepare(world, pos, i, maxUpdateDepth - 1);
		newState.updateNeighbors(world, pos, i, maxUpdateDepth - 1);
		newState.prepare(world, pos, i, maxUpdateDepth - 1);
	}
	
	private static void forEachPos(CommandContext<ServerCommandSource> ct, BiConsumer<BlockPos, ServerWorld> action) 
			throws CommandSyntaxException {
		BlockPos from = BlockPosArgumentType.getBlockPos(ct, "from");
		BlockPos to = BlockPosArgumentType.getBlockPos(ct, "to");
		ServerWorld world = ct.getSource().getWorld();
		for (BlockPos pos : BlockPos.iterate(from, to)) {
			action.accept(pos, world);
		}
	}
}
