package mc.lovexyn0827.mcwmem.rendering;

import mc.lovexyn0827.mcwmem.command.CommandUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ServerSyncedBoxRenderer {
	private MinecraftClient client;
	private MinecraftServer server;
	
	public ServerSyncedBoxRenderer() {
		this.client = MinecraftClient.getInstance();
	}

	public void setServerAndInitialize(MinecraftServer server) {
		this.server = server ;
		server.getCommandManager().execute(CommandUtil.noreplySource(), 
				"/script load box_renderer");
	}
	
	private void updateBox(Vec3d pos,ServerWorld world) {
		for(Entity entity:world.getEntitiesByClass(Entity.class, new net.minecraft.util.math.Box(pos, pos).expand(64), (e)->true)) {
			if(entity instanceof ServerPlayerEntity) continue;
				world.getServer().getCommandManager().execute(CommandUtil.noreplySource(), 
						"/script in box_render invoke render_box "+entity.getX()+" "+entity.getY()+" "+entity.getZ()+" "+entity.getBoundingBox().getXLength()+" "+entity.getBoundingBox().getYLength());
		}
	}
	
	public void tick() {
		if(this.server==null||this.client.player==null) return;
		this.server.getWorlds().forEach((world)->this.updateBox(this.client.player.getPos(), world));
	}

	public void uninitialize() {
		this.server = null;
	}
}
