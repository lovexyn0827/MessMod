package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.function.LongConsumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;

public class LazyLoadCommand {
	public static final LongSet LAZY_CHUNKS = new LongOpenHashSet();
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("lazyload").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("add")
						.then(argument("corner1", ColumnPosArgumentType.columnPos())
								.executes((ct) -> {
									ColumnPos pos = ColumnPosArgumentType.getColumnPos(ct, "corner1");
									LAZY_CHUNKS.add(ChunkPos.toLong(pos.x() >> 4, pos.z() >> 4));
									CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("corner2", ColumnPosArgumentType.columnPos())
										.executes((ct) -> {
											forEachSelected(ct, LAZY_CHUNKS::add);
											CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										}))))
				.then(literal("remove")
						.then(argument("corner1", ColumnPosArgumentType.columnPos())
								.executes((ct) -> {
									ColumnPos pos = ColumnPosArgumentType.getColumnPos(ct, "corner1");
									LAZY_CHUNKS.remove(ChunkPos.toLong(pos.x() >> 4, pos.z() >> 4));
									CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("corner2", ColumnPosArgumentType.columnPos())
										.executes((ct) -> {
											forEachSelected(ct, LAZY_CHUNKS::remove);
											CommandUtil.feedbackWithArgs(ct, "cmd.general.success");
											return Command.SINGLE_SUCCESS;
										}))));
		dispatcher.register(command);
	}
	
	public static void reset() {
		LAZY_CHUNKS.clear();
	}
	
	private static void forEachSelected(CommandContext<ServerCommandSource> ct, LongConsumer action) {
		ColumnPos c1 = ColumnPosArgumentType.getColumnPos(ct, "corner1");
		ColumnPos c2 = ColumnPosArgumentType.getColumnPos(ct, "corner2");
		int x1 = Math.min(c1.x(), c2.x()) >> 4;
		int x2 = Math.max(c1.x(), c2.x()) >> 4;
		int z1 = Math.min(c1.z(), c2.z()) >> 4;
		int z2 = Math.min(c1.z(), c2.z()) >> 4;
		for(int x = x1; x <= x2; x++) {
			for(int z = z1; z <= z2; z++) {
				action.accept(ChunkPos.toLong(x, z));
			}
		}
	}
}
