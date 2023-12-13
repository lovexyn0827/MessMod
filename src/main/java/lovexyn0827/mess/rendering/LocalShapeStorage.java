package lovexyn0827.mess.rendering;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Stores shapes in single player games.
 */
@Environment(EnvType.CLIENT)
public class LocalShapeStorage extends ShapeCache implements ShapeSender {
	private final UUID localPlayerUuid;
	
	LocalShapeStorage() {
		this.localPlayerUuid = MinecraftClient.getInstance().getSession().getUuidOrNull();
	}

	@Override
	public synchronized void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space, 
			ServerPlayerEntity player) {
		if(player == null || player.getUuid().equals(this.localPlayerUuid)) {
			Set<Shape> set = this.getShapesInDimension(dim)
					.computeIfAbsent(space, (ss) -> Sets.newHashSet());
			set.add(shape);
		}
	}

	@Override
	public synchronized void clearSpaceFromServer(ShapeSpace space, ServerPlayerEntity player) {
		if(player == null || player.getUuid().equals(this.localPlayerUuid)) {
			this.clearSpace(space);
		}
	}

	@Override
	public void updateClientTime(long gt) {
		this.time = gt;
	}
}
