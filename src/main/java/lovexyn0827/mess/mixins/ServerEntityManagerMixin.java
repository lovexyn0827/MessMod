package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import lovexyn0827.mess.command.LazyLoadCommand;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.util.math.ChunkPos;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {
	@ModifyVariable(method = "updateTrackingStatus", at = @At("HEAD"), argsOnly = true)
	private ChunkHolder.LevelType modifyToLazyLoadedIfNeeded(ChunkHolder.LevelType type, 
			ChunkPos chunkPos, ChunkHolder.LevelType levelType) {
		if(!LazyLoadCommand.LAZY_CHUNKS.isEmpty()) {
			long pos = chunkPos.toLong();
			if(LazyLoadCommand.LAZY_CHUNKS.contains(pos) && levelType.isAfter(ChunkHolder.LevelType.ENTITY_TICKING)) {
				return ChunkHolder.LevelType.TICKING;
			}
		}
		
		return levelType;
	}
}
