package lovexyn0827.mess.rendering;

import java.util.List;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

// FIXME Boxes are still rendered when the corresponding entities have already been removed
public class ServerSyncedBoxRenderer {
	private MinecraftServer server;
	private long lastUpdated;
	
	public ServerSyncedBoxRenderer() {
	}

	public void setServer(MinecraftServer server) {
		this.server = server ;
	}
	
	private void updateBox(Vec3d pos, ServerWorld world) {
		ShapeSender sr = MessMod.INSTANCE.shapeSender;
		this.lastUpdated = world.getTime();
		float r = OptionManager.serverSyncedBoxRenderRange;
		List<? extends Entity> list;
		if(r > 0) {
			list = world.getEntitiesByClass(Entity.class, new net.minecraft.util.math.Box(pos, pos).expand(r), (e) -> true);
		} else {
			list = world.getEntitiesByType(null, (e) -> true);
		}
		
		for(Entity entity : list) {
			if(entity instanceof ServerPlayerEntity) continue;
			sr.addShape(new RenderedBox(entity.getBoundingBox(), 0x31f38bFF, 0, 0, world.getTime()), world.getRegistryKey());
		}
	}
	
	public void tick() {
		if(this.server.getOverworld().getTime() == this.lastUpdated) return;
		if(this.server == null || !OptionManager.serverSyncedBox) return;
		for(PlayerEntity player : server.getPlayerManager().getPlayerList())
		this.server.getWorlds().forEach((world) -> this.updateBox(player.getPos(), world));
	}

	public void uninitialize() {
		this.server = null;
	}
}