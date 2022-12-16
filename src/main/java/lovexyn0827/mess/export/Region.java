package lovexyn0827.mess.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import lovexyn0827.mess.mixins.MinecraftServerAccessor;
import lovexyn0827.mess.mixins.RegionBasedStorageAccessor;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;
import net.minecraft.world.storage.RegionBasedStorage;

public class Region {
	private final ChunkPos max;
	private final ChunkPos min;
	private final ServerWorld dimension;

	public Region(ChunkPos corner1, ChunkPos corner2, ServerWorld dimension) {
		this.max = new ChunkPos(Math.max(corner1.x, corner2.x), Math.max(corner1.z, corner2.z));
		this.min = new ChunkPos(Math.min(corner1.x, corner2.x), Math.min(corner1.z, corner2.z));
		this.dimension = dimension;
	}

	public BlockBox getBlockBox() {
		return new BlockBox(this.min.getStartPos(), this.max.getStartPos().add(15, 255, 15));
	}

	public ServerWorld getWorld() {
		return this.dimension;
	}

	public void export(Path temp) throws IOException {
		Path dir = this.dimension.getServer().getSavePath(WorldSavePathMixin.create(""))
				.relativize(((MinecraftServerAccessor) this.dimension.getServer()).getSession()
						.getWorldDirectory(this.dimension.getRegistryKey())
						.toPath());
		RegionBasedStorage storage = RegionBasedStorageAccessor.create(new File(temp.resolve(dir).toFile(), "region"), true);
		PointOfInterestStorage poiStorage = this.dimension.getPointOfInterestStorage();
		PointOfInterestStorage poiDst = new PointOfInterestStorage(new File(temp.resolve(dir).toFile(), "poi"), 
				this.dimension.getServer().getDataFixer(), false);
		for(int x = this.min.x; x <= this.max.x; x++) {
			for(int z = this.min.z; z <= this.max.z; z++) {
				CompoundTag tag = ChunkSerializer.serialize(this.dimension, this.dimension.getChunk(x, z));
				ChunkPos pos = new ChunkPos(x, z);
				((RegionBasedStorageAccessor)(Object) storage).writeChunk(pos, tag);
				// FIXME: Exporting POIs & Raids
				poiStorage.getInChunk((poi) -> true, pos, OccupationStatus.ANY).forEach((poi) -> {
					poiDst.add(poi.getPos(), poi.getType());
				});;
			}
		}
		
		storage.close();
		poiDst.tick(() -> true);
		poiDst.close();
	}

}
