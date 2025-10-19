package lovexyn0827.mess.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;

@Mixin(StorageIoWorker.class)
public interface StorageIoWorkerAccessor {
	@Accessor("results")
	Map<ChunkPos, Object> getResultsMessMod();
}
