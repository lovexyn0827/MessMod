package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ServerSyncedBoxRenderer {
	private MinecraftClient client;
	private MinecraftServer server;
	private long lastUpdated;
	
	public ServerSyncedBoxRenderer() {
		this.client = MinecraftClient.getInstance();
	}

	public void setServer(MinecraftServer server) {
		this.server = server ;
	}
	
	private void updateBox(Vec3d pos,ServerWorld world) {
		this.lastUpdated = world.getTime();
		for(Entity entity:world.getEntitiesByClass(Entity.class, new net.minecraft.util.math.Box(pos, pos).expand(128), (e) -> true)) {
			if(entity instanceof ServerPlayerEntity) continue;
			MessMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(entity.getBoundingBox(), 0x31f38bFF, 0, 1), world.getRegistryKey());
			Vec3d ePos = entity.getPos();
			MessMod.INSTANCE.shapeRenderer.addShape(new RenderedLine(ePos, ePos, 0xFFFF00FF, 1), world.getRegistryKey());
			Vec3d eye = new Vec3d(ePos.x, entity.getEyeY(), ePos.z);
			MessMod.INSTANCE.shapeRenderer.addShape(new RenderedLine(eye, eye, 0xFF00FFFF, 1), world.getRegistryKey());
		}
	}
	
	public void tick() {
		if(this.server.getOverworld().getTime() == this.lastUpdated) return;
		if(this.server==null || this.client.player==null || !OptionManager.serverSyncedBox) return;
		this.server.getWorlds().forEach((world) -> this.updateBox(this.client.player.getPos(), world));
	}

	public void uninitialize() {
		this.server = null;
	}
}
