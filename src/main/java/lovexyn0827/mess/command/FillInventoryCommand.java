package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class FillInventoryCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("fillinventory").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("corner1", BlockPosArgumentType.blockPos())
						.then(argument("corner2", BlockPosArgumentType.blockPos())
								.then(literal("full")
										.then(argument("item", ItemStackArgumentType.itemStack(cra))
												.executes((ct) -> {
													BlockPos p1 = BlockPosArgumentType.getBlockPos(ct, "corner1");
													BlockPos p2 = BlockPosArgumentType.getBlockPos(ct, "corner2");
													ItemStackArgument stackArg = ItemStackArgumentType.getItemStackArgument(ct, "item");
													ItemStack stack = stackArg.createStack(stackArg.getItem().getMaxCount(), false);
													for (BlockPos pos : BlockPos.iterate(p1, p2)) {
														BlockEntity be = ct.getSource().getWorld().getBlockEntity(pos);
														if (be instanceof Inventory) {
															Inventory inv = (Inventory) be;
															int size = inv.size();
															for (int i = 0; i < size; i++) {
																inv.setStack(i, stack.copy());
															}
														}
													}

													CommandUtil.feedback(ct, "cmd.general.success");
													return Command.SINGLE_SUCCESS;
												})))
								.then(literal("replace")
										.then(argument("from", ItemStackArgumentType.itemStack(cra))
												.then(argument("to", ItemStackArgumentType.itemStack(cra))
														.executes((ct) -> {
															BlockPos p1 = BlockPosArgumentType.getBlockPos(ct, "corner1");
															BlockPos p2 = BlockPosArgumentType.getBlockPos(ct, "corner2");
															Item from = ItemStackArgumentType.getItemStackArgument(ct, "from").getItem();
															ItemStackArgument to = ItemStackArgumentType.getItemStackArgument(ct, "to");
															for (BlockPos pos : BlockPos.iterate(p1, p2)) {
																BlockEntity be = ct.getSource().getWorld().getBlockEntity(pos);
																if (be instanceof Inventory) {
																	Inventory inv = (Inventory) be;
																	int size = inv.size();
																	for (int i = 0; i < size; i++) {
																		ItemStack cur = inv.getStack(i);
																		if (cur.getItem() == from) {
																			inv.setStack(i, to.createStack(cur.getCount(), true));
																		}
																	}
																}
															}

															CommandUtil.feedback(ct, "cmd.general.success");
															return Command.SINGLE_SUCCESS;
														}))))
								.then(literal("sorter")
										.then(argument("main", ItemStackArgumentType.itemStack(cra))
												.then(argument("occupier", ItemStackArgumentType.itemStack(cra))
														.executes((ct) -> {
															BlockPos p1 = BlockPosArgumentType.getBlockPos(ct, "corner1");
															BlockPos p2 = BlockPosArgumentType.getBlockPos(ct, "corner2");
															ItemStack main = ItemStackArgumentType
																	.getItemStackArgument(ct, "main")
																	.createStack(1, true);
															ItemStack occupier = ItemStackArgumentType
																	.getItemStackArgument(ct, "occupier")
																	.createStack(1, true);
															for (BlockPos pos : BlockPos.iterate(p1, p2)) {
																BlockEntity be = ct.getSource().getWorld().getBlockEntity(pos);
																if (be instanceof Inventory) {
																	Inventory inv = (Inventory) be;
																	inv.setStack(0, main.copy());
																	int size = inv.size();
																	for (int i = 1; i < size; i++) {
																		inv.setStack(i, occupier.copy());
																	}
																}
															}

															CommandUtil.feedback(ct, "cmd.general.success");
															return Command.SINGLE_SUCCESS;
														}))))));
		dispatcher.register(command);
	}
}
