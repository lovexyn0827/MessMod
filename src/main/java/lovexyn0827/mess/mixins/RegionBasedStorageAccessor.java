package lovexyn0827.mess.mixins;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;

@Mixin(RegionBasedStorage.class)
public interface RegionBasedStorageAccessor {
	@Invoker("<init>")
	public static RegionBasedStorage create(File directory, boolean dsync) {
		throw new AssertionError();
	}
	
	@Invoker("write")
	void writeChunk(ChunkPos pos, CompoundTag tag);
}
