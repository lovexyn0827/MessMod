package mc.lovexyn0827.mcwmem.rendering;

import mc.lovexyn0827.mcwmem.MCWMEMod;
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
		if(world.getTime() == this.lastUpdated) return;
		this.lastUpdated = world.getTime();
		for(Entity entity:world.getEntitiesByClass(Entity.class, new net.minecraft.util.math.Box(pos, pos).expand(128), (e) -> true)) {
			if(entity instanceof ServerPlayerEntity) continue;
				MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedBox(entity.getBoundingBox(), 0x31f38bFF, 0, 1), world.getRegistryKey());
				Vec3d ePos = entity.getPos();
				MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedLine(ePos, ePos, 0xFFFF00FF, 1), world.getRegistryKey());
				Vec3d eye = new Vec3d(ePos.x, entity.getEyeY(), ePos.z);
				MCWMEMod.INSTANCE.shapeRenderer.addShape(new RenderedLine(eye, eye, 0xFF00FFFF, 1), world.getRegistryKey());
		}
	}
	
	public void tick() {
		if(this.server==null || this.client.player==null || !MCWMEMod.INSTANCE.getBooleanOption("serverSyncedBox")) return;
		this.server.getWorlds().forEach((world) -> this.updateBox(this.client.player.getPos(), world));
	}

	public void uninitialize() {
		this.server = null;
	}
}
