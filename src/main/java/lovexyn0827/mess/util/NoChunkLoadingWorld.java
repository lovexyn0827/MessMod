package lovexyn0827.mess.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import lovexyn0827.mess.fakes.ThreadedAnvilChunkStorageInterface;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.EntityView;
import net.minecraft.world.chunk.WorldChunk;

/**
 * A world-like object without the ability of loading chunks, modifying chunk caches, etc.
 */
public final class NoChunkLoadingWorld implements EntityView, BlockView {
	private final ServerWorld world;
	private final ServerChunkManager scm;
	private final ThreadedAnvilChunkStorage tacs;

	public NoChunkLoadingWorld(ServerWorld world) {
		this.world = world;
		this.scm = world.getChunkManager();
		this.tacs = this.scm.threadedAnvilChunkStorage;
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
		return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).getFluidState(pos);
	}
	
	/**
	 * Modified version of {@code net.minecraft.world.World#getOtherEntities(Entity, Box, Predicate)}.
	 */
	@Override
	public List<Entity> getOtherEntities(Entity except, Box box, Predicate<? super Entity> predicate) {
		int i = MathHelper.floor((box.minX - 2.0) / 16.0);
        int j = MathHelper.ceil((box.maxX + 2.0) / 16.0);
        int k = MathHelper.floor((box.minZ - 2.0) / 16.0);
        int l = MathHelper.ceil((box.maxZ + 2.0) / 16.0);
        ArrayList<Entity> list = Lists.newArrayList();
        for (int m = i; m < j; ++m) {
            for (int n = k; n < l; ++n) {
                WorldChunk worldChunk = this.getChunk(m, n);
                if (worldChunk == null) {
                	continue;
                }
                
                worldChunk.collectOtherEntities(except, box, list, predicate);
            }
        }
        
        return list;
	}

	/**
	 * Modified version {@code net.minecraft.world.World#getEntitiesByClass(Class, Box, Predicate)}.
	 */
	@Override
	public <T extends Entity> List<T> getEntitiesByClass(Class<? extends T> clazz, Box box, 
			Predicate<? super T> predicate) {
        int i = MathHelper.floor((box.minX - 2.0) / 16.0);
        int j = MathHelper.ceil((box.maxX + 2.0) / 16.0);
        int k = MathHelper.floor((box.minZ - 2.0) / 16.0);
        int l = MathHelper.ceil((box.maxZ + 2.0) / 16.0);
        ArrayList<T> list = Lists.newArrayList();
        for (int m = i; m < j; ++m) {
            for (int n = k; n < l; ++n) {
                WorldChunk worldChunk = this.getChunk(m, n);
                if (worldChunk == null) {
                	continue;
                }
                
                worldChunk.collectEntitiesByClass(clazz, box, list, predicate);
            }
        }
        
        return list;
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return this.world.getPlayers();
	}
}
