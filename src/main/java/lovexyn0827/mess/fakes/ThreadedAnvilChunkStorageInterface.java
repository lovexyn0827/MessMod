package lovexyn0827.mess.fakes;

import net.minecraft.server.world.ChunkHolder;

public interface ThreadedAnvilChunkStorageInterface {
	ChunkHolder getCHForMessMod(long pos);
}
