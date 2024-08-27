package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;

public class TileEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("tileentity").requires(CommandUtil.COMMAND_REQUMENT).
				then(literal("get").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct) -> {
									BlockEntity be = ct.getSource().getWorld().getBlockEntity(BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
									if(be == null) {
										CommandUtil.feedback(ct, "null");
										return -1;
									}
									
									CommandUtil.feedbackWithArgs(ct, "cmd.tileentity.type", Registries.BLOCK_ENTITY_TYPE.getId(be.getType()).getPath());
									CommandUtil.feedbackWithArgs(ct, "cmd.tileentity.data", be.createNbtWithId(be.getWorld().getRegistryManager()));
									return 1;
								}))).
				then(literal("remove").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									ct.getSource().getWorld().removeBlockEntity(BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
									CommandUtil.feedback(ct, "cmd.general.success");
									return 1;
								})));
		dispatcher.register(command);
	}
}
