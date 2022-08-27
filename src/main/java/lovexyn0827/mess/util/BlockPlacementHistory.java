package lovexyn0827.mess.util;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BlockPlacementHistory {
	private Map<ServerPlayerEntity, Stack<ImmutableTriple<BlockPos, BlockState, BlockState>>> history = Maps.newHashMap();
	private Map<ServerPlayerEntity, Stack<ImmutableTriple<BlockPos, BlockState, BlockState>>> redoQueue = Maps.newHashMap();

	public void push(ServerPlayerEntity player, BlockPos pos, BlockState prevState, BlockState newState) {
		this.history.computeIfAbsent(player, (p) -> new Stack<>())
			.push(new ImmutableTriple<>(pos, prevState, newState));
		this.redoQueue.computeIfAbsent(player, (p) -> new Stack<>()).clear();
	}
	
	public void undo(ServerPlayerEntity player) {
		Stack<ImmutableTriple<BlockPos, BlockState, BlockState>> stack = this.history.computeIfAbsent(player, (p) -> new Stack<>());
		if(stack.empty())	return;
		ImmutableTriple<BlockPos, BlockState, BlockState> record = stack.pop();
		player.world.setBlockState(record.left, record.middle, 11, 0);
		this.redoQueue.computeIfAbsent(player, (p) -> new Stack<>()).push(record);
	}
	
	public void redo(ServerPlayerEntity player) {
		Stack<ImmutableTriple<BlockPos, BlockState, BlockState>> stack = this.redoQueue.computeIfAbsent(player, (p) -> new Stack<>());
		if(stack.empty())	return;
		ImmutableTriple<BlockPos, BlockState, BlockState> record = stack.pop();
		player.world.setBlockState(record.left, record.right, 11, 0);
		this.history.computeIfAbsent(player, (p) -> new Stack<>()).push(record);
	}
}
