package lovexyn0827.mess.util;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.fakes.ThreadedAnvilChunkStorageInterface;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.EntityView;
import net.minecraft.world.chunk.WorldChunk;

/**
 * A world-like object without the ability of loading chunks, modifying chunk caches, etc.
 */
public final class NoChunkLoadingWorld implements EntityView, BlockView {
	private final ServerWorld world;
	private final ServerChunkManager scm;
	private final ServerChunkLoadingManager tacs;

	public NoChunkLoadingWorld(ServerWorld world) {
		this.world = world;
		this.scm = world.getChunkManager();
		this.tacs = this.scm.chunkLoadingManager;
	}
	
	@Nullable
	private WorldChunk getChunk(int x, int z) {
		ChunkHolder ch = ((ThreadedAnvilChunkStorageInterface) this.tacs).getCHForMessMod(ChunkPos.toLong(x, z));
		if (ch == null || !ch.isAccessible()) {
			return null;
		}
		
		return ch.getWorldChunk();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		WorldChunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk != null ? chunk.getBlockEntity(pos) : null;
	}

	@Override
	@NotNull
	public BlockState getBlockState(BlockPos pos) {
		WorldChunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk != null ? chunk.getBlockState(pos) : Blocks.VOID_AIR.getDefaultState();
	}

	@Override
	@NotNull
	public FluidState getFluidState(BlockPos pos) {
		WorldChunk chunk = this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		return chunk != null ? chunk.getFluidState(pos) : Fluids.EMPTY.getDefaultState();
	}
	
	/**
	 * Modified version of {@code net.minecraft.world.World#getOtherEntities(Entity, Box, Predicate)}.
	 */
	@Override
	public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
		// Simply delegating to underlying world should be fine since the entity storage is independent.
		return this.world.getOtherEntities(except, box, predicate);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> type, Box box,
			Predicate<? super T> predicate) {
		return this.world.getEntitiesByType(type, box, predicate);
	}

	/**
	 * Modified version {@code net.minecraft.world.World#getEntitiesByClass(Class, Box, Predicate)}.
	 */
	@Override
	public <T extends Entity> List<T> getEntitiesByClass(Class<T> clazz, Box box, 
			Predicate<? super T> predicate) {
        return this.world.getEntitiesByClass(clazz, box, predicate);
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return this.world.getPlayers();
	}

	@Override
	public int getHeight() {
		return this.world.getHeight();
	}

	@Override
	public int getBottomY() {
		return this.world.getBottomY();
	}
}
