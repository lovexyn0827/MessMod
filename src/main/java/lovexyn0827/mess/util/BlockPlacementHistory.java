package lovexyn0827.mess.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Objects;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockPlacementHistory {
	private static final ThreadLocal<List<BlockChange>> CURRENT = new ThreadLocal<>();
	private static final ThreadLocal<NbtCompound> NEXT_BE = new ThreadLocal<>();
	private final ServerPlayerEntity player;
	private final Stack<Operation> history = new Stack<>();
	private final Stack<Operation> redoQueue = new Stack<>();
	
	public BlockPlacementHistory(ServerPlayerEntity player) {
		this.player = player;
	}

	public void pushSingle(BlockPos pos, BlockState prevState, 
			BlockState newState, @Nullable BlockEntity prevBlockEntity) {
		this.history.push(new Operation(
				Collections.singletonList(new BlockChange(this.player.getServerWorld(), pos, prevState, 
				prevBlockEntity == null ? null : prevBlockEntity.writeNbt(new NbtCompound()), newState, null))));
		this.redoQueue.clear();
	}
	
	public void beginOperation() {
		CURRENT.set(new ArrayList<>());
	}
	
	public static void appendBlockChange(ServerWorld world, BlockPos pos, BlockState prevState, 
			BlockState newState, @Nullable NbtCompound prevBlockEntity, @Nullable NbtCompound newBlockEntity) {
		if(prevState.equals(newState) && Objects.equal(prevBlockEntity, newBlockEntity)) {
			return;
		}
		
		if(CURRENT.get() == null) {
			return;
		}
		
		NbtCompound be = NEXT_BE.get();
		CURRENT.get().add(new BlockChange(world, pos.toImmutable(), 
				prevState, be == null ? prevBlockEntity : be, 
				newState, newBlockEntity));
	}
	
	public void preparePrevBlockEntityForTheNext(BlockEntity be) {
		if(be != null) {
			NEXT_BE.set(be.writeNbt(new NbtCompound()));
		}
	}
	
	public void endOperation(boolean abort) {
		if(abort) {
			CURRENT.set(null);
		} else {
			this.history.push(new Operation(CURRENT.get()));
			this.redoQueue.clear();
			CURRENT.set(null);
		}
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
	
	private static final class BlockChange {
		protected final BlockPos pos;
		protected final BlockState prevState;
		protected final BlockState newState;
		@Nullable
		protected final NbtCompound prevBlockEntity;
		@Nullable
		protected final NbtCompound newBlockEntity;
		private final ServerWorld world;
		
		BlockChange(ServerWorld world, BlockPos pos, BlockState prevState, NbtCompound prevBlockEntity, 
				BlockState newState, NbtCompound newBlockEntity) {
			this.world = world;
			this.pos = pos;
			this.prevState = prevState;
			this.newState = newState;
			this.prevBlockEntity = prevBlockEntity;
			this.newBlockEntity = newBlockEntity;
		}

		public void redo() {
			this.world.setBlockState(this.pos, this.newState, 2, 0);
			if(this.newBlockEntity != null) {
				BlockEntity be = this.world.getBlockEntity(this.pos);
				if(be != null) {
					be.readNbt(appendLocationalData(this.newBlockEntity, this.pos));
				}
			}
		}

		public void undo() {
			this.world.setBlockState(this.pos, this.prevState, 2, 0);
			if (this.prevBlockEntity != null) {
				BlockEntity be = this.world.getBlockEntity(this.pos);
				if(be != null) {
					be.readNbt(appendLocationalData(this.prevBlockEntity, this.pos));
				}
			}
		}
		
		private static NbtCompound appendLocationalData(NbtCompound tag, BlockPos pos) {
			tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            return tag;
		}
	}
}
