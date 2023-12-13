package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import lovexyn0827.mess.command.LazyLoadCommand;
import lovexyn0827.mess.fakes.ServerEntityManagerInterface;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin implements ServerEntityManagerInterface {
	@Unique
	private ServerWorld world;
	
	@ModifyVariable(method = "updateTrackingStatus", at = @At("HEAD"), argsOnly = true)
	private ChunkHolder.LevelType modifyToLazyLoadedIfNeeded(ChunkHolder.LevelType type, 
			ChunkPos chunkPos, ChunkHolder.LevelType levelType) {
		if(!LazyLoadCommand.LAZY_CHUNKS.isEmpty()) {
			long pos = chunkPos.toLong();
			if(LazyLoadCommand.LAZY_CHUNKS.containsKey(this.world.getRegistryKey())
					&& LazyLoadCommand.LAZY_CHUNKS.get(this.world.getRegistryKey()).contains(pos)) {
				return ChunkHolder.LevelType.TICKING;
			}
		}
		
		return levelType;
	}

	@Override
	public void initWorld(ServerWorld world) {
		this.world = world;
	}
}
