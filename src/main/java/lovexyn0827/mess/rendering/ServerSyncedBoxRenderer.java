package lovexyn0827.mess.rendering;

import java.util.ArrayList;
import java.util.List;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.ServerWorldInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.CarpetUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ServerSyncedBoxRenderer {
	private static final ShapeSpace ENTITY_BOX_SPACE = new ShapeSpace("entity_box");
	private final MinecraftServer server;
	
	public ServerSyncedBoxRenderer(MinecraftServer server) {
		this.server = server;
	}
	
	private void addBoxes(ServerPlayerEntity player, boolean frozen, FrozenUpdateMode mode) {
		ShapeSender sr = MessMod.INSTANCE.shapeSender;
		float r = OptionManager.serverSyncedBoxRenderRange;
		ServerWorld world = player.getServerWorld();
		if(!(frozen && mode == FrozenUpdateMode.NO_REMOVAL)) {
			MessMod.INSTANCE.shapeSender.clearSpaceFromServer(ENTITY_BOX_SPACE, player);
		}
		
		List<Entity> list;
		if(r > 0) {
			Vec3d pos = player.getPos();
			if (OptionManager.directChunkAccessForMessMod) {
				list = ((ServerWorldInterface) world).toNoChunkLoadingWorld()
						.getEntitiesByClass(Entity.class, new Box(pos, pos).expand(r), (e) -> true);
			} else {
				list = world.getEntitiesByClass(Entity.class, new Box(pos, pos).expand(r), (e) -> true);
			}
		} else {
			list = new ArrayList<Entity>();
			world.iterateEntities().forEach(list::add);
		}
		
		for(Entity entity : list) {
			if(entity instanceof ServerPlayerEntity) continue;
			sr.addShape(new RenderedBox(entity.getBoundingBox(), 0x31f38bFF, 0, 0, world.getTime()), 
					world.getRegistryKey(), ENTITY_BOX_SPACE, player);
		}
	}
	
	public void tick() {
		if(this.server == null || !OptionManager.serverSyncedBox) {
			return;
		}
		
		boolean frozen = CarpetUtil.isTickFrozen();
		FrozenUpdateMode mode = OptionManager.serverSyncedBoxUpdateModeInFrozenTicks;
		if(frozen && mode == FrozenUpdateMode.PAUSE) {
			return;
		}
		
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if(player.getServerWorld() != null) {
				this.addBoxes(player, frozen, mode);
			}
		}
	}
}
