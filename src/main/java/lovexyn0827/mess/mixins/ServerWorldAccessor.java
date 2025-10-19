package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
	@Accessor("entityManager")
	ServerEntityManager<Entity> getEntityManager();
}
