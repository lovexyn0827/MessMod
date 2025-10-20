package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.StorageIoWorker;

@Mixin(ChunkPosKeyedStorage.class)
public interface ChunkPosKeyedStorageAccessor {
	@Accessor("worker")
	StorageIoWorker getStorageIoWorkerMessMod();
}
