package lovexyn0827.mess.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;

import lovexyn0827.mess.mixins.MinecraftServerAccessor;
import lovexyn0827.mess.mixins.RegionBasedStorageAccessor;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;
import net.minecraft.world.storage.RegionBasedStorage;

public final class Region {
	private final ChunkPos max;
	private final ChunkPos min;
	private final ServerWorld dimension;
	private final String name;

	public Region(String name, ChunkPos corner1, ChunkPos corner2, ServerWorld dimension) {
		this.max = new ChunkPos(Math.max(corner1.x, corner2.x), Math.max(corner1.z, corner2.z));
		this.min = new ChunkPos(Math.min(corner1.x, corner2.x), Math.min(corner1.z, corner2.z));
		this.dimension = dimension;
		this.name = name;
	}

	public BlockBox getBlockBox() {
	    BlockPos minPos = this.min.getStartPos();
	    BlockPos maxPos = this.max.getStartPos().add(15, 255, 15);
		return new BlockBox(minPos.getX(), minPos.getY(), minPos.getZ(), maxPos.getX(), maxPos.getY(), maxPos.getZ());
	}

	public String getName() {
		return this.name;
	}

	public ServerWorld getWorld() {
		return this.dimension;
	}

	public void export(Path temp, EnumSet<SaveComponent> components) throws IOException {
		Path dir = this.dimension.getServer().getSavePath(WorldSavePathMixin.create(""))
				.relativize(((MinecraftServerAccessor) this.dimension.getServer()).getSession()
						.getWorldDirectory(this.dimension.getRegistryKey()));
		RegionBasedStorage storage = RegionBasedStorageAccessor
				.create(temp.resolve(dir).resolve("region"), true);
		PointOfInterestStorage poiStorage = this.dimension.getPointOfInterestStorage();
		PointOfInterestStorage poiDst = new PointOfInterestStorage(temp.resolve(dir).resolve("poi"), 
				this.dimension.getServer().getDataFixer(), false, this.dimension);
		for(int x = this.min.x; x <= this.max.x; x++) {
			for(int z = this.min.z; z <= this.max.z; z++) {
				ChunkPos pos = new ChunkPos(x, z);
				if(components.contains(SaveComponent.REGION)) {
					NbtCompound tag = ChunkSerializer.serialize(this.dimension, this.dimension.getChunk(x, z));
					((RegionBasedStorageAccessor)(Object) storage).writeChunk(pos, tag);
				}
				
				if(components.contains(SaveComponent.POI)) {
					poiStorage.getInChunk((poi) -> true, pos, OccupationStatus.ANY).forEach((poi) -> {
						poiDst.add(poi.getPos(), poi.getType());
					});
				}
			}
		}
		
		storage.close();
		// FIXME: POIs may be exported incorrectly
		poiDst.tick(() -> true);
		poiDst.close();
	}

	public boolean contains(ServerWorld world, BlockPos pos) {
		return this.dimension == world && this.min.x << 4 <= pos.getX() && this.max.x << 4  + 15 >= pos.getX()
				&& this.min.z << 4 <= pos.getZ() && this.max.z << 4  + 15 >= pos.getZ();
	}

	boolean contains(MapState ms) {
		int scale = 1 << ms.scale;
		int r = 63 * scale;
		int x0 = this.min.x << 4;
		int z0 = this.min.z << 4;
		int x1 = this.max.x << 4 + 15;
		int z1 = this.max.z << 4 + 15;
		return this.dimension.getRegistryKey().equals(ms.dimension)
				&& ms.centerX + r >= x0 && ms.centerX - r <= x1 && ms.centerZ + r > z0 && ms.centerZ < z1;
	}

	public boolean contains(ServerWorld world, long pos) {
		int x = ChunkPos.getPackedX(pos);
		int z = ChunkPos.getPackedZ(pos);
		return this.dimension == world && this.min.x <= x && this.max.x >= x
				&& this.min.z <= z && this.max.z >= z;
	}
	
	@Override
	public String toString() {
		return new StringBuilder(this.name)
				.append('{').append(this.min).append('-').append(this.max).append('}').toString();
	}
}
