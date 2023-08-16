package lovexyn0827.mess.util;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockPlacementHistory {
	private final ServerPlayerEntity player;
	private final Stack<Operation> history = new Stack<>();
	private final Stack<Operation> redoQueue = new Stack<>();
	
	public BlockPlacementHistory(ServerPlayerEntity player) {
		this.player = player;
	}

	public void pushSingle(BlockPos pos, BlockState prevState, 
			BlockState newState, @Nullable BlockEntity prevBlockEntity) {
		this.history.push(new Operation(Collections.singletonList(new  BlockChange(pos, prevState, 
				prevBlockEntity == null ? null : prevBlockEntity.toTag(new CompoundTag()), newState, null))));
		this.redoQueue.clear();
	}
	
	public void undo() {
		if(this.history.empty()) {
			return;
		}
		
		Operation o = this.history.pop();
		o.undo();
		this.redoQueue.push(o);
	}
	
	public void redo() {
		if(this.redoQueue.empty()) {
			return;
		}
		
		Operation o = this.redoQueue.pop();
		o.redo();
		this.history.push(o);
	}
	
	private final class Operation {
		private final List<BlockChange> changed;
		
		protected Operation(List<BlockChange> changed) {
			this.changed = changed;
		}
		
		void redo() {
			for(BlockChange bc : this.changed) {
				bc.redo();
			}
		}
		
		void undo() {
			for(BlockChange bc : this.changed) {
				bc.undo();
			}
		}
	}
	
	private final class BlockChange {
		protected final BlockPos pos;
		protected final BlockState prevState;
		protected final BlockState newState;
		@Nullable
		protected final CompoundTag prevBlockEntity;
		@Nullable
		protected final CompoundTag newBlockEntity;
		
		BlockChange(BlockPos pos, BlockState prevState, CompoundTag prevBlockEntity, 
				BlockState newState, CompoundTag newBlockEntity) {
			this.pos = pos;
			this.prevState = prevState;
			this.newState = newState;
			this.prevBlockEntity = prevBlockEntity;
			this.newBlockEntity = newBlockEntity;
		}

		public void redo() {
			ServerWorld world = BlockPlacementHistory.this.player.getServerWorld();
			world.setBlockState(this.pos, this.newState, 11, 0);
			if(this.newBlockEntity != null) {
				world.setBlockEntity(this.pos, BlockEntity.createFromTag(this.newState, this.newBlockEntity));
			}
		}

		public void undo() {
			ServerWorld world = BlockPlacementHistory.this.player.getServerWorld();
			world.setBlockState(this.pos, this.prevState, 11, 0);
			if (this.prevBlockEntity != null) {
				world.setBlockEntity(this.pos, BlockEntity.createFromTag(this.prevState, this.prevBlockEntity));
			}
		}
	}
}
