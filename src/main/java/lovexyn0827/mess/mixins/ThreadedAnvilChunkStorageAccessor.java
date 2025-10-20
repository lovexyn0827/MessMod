package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;

@Mixin(ServerChunkLoadingManager.class)
public interface ThreadedAnvilChunkStorageAccessor {
	@Invoker("getChunkHolder")
	ChunkHolder getCH(long pos);
}
