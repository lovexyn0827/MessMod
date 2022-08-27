package lovexyn0827.mess.rendering;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class LocalShapeStorage extends ShapeCache implements ShapeSender {
	LocalShapeStorage() {
	}

	@SuppressWarnings("unused")
	@Override
	public synchronized void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space) {
		Set<Shape> set = this.getShapesInDimension(dim)
				.computeIfAbsent(space, (ss) -> Sets.newHashSet());
		Map<?, ?> map = this.getShapesInDimension(dim);
		set.add(shape);
	}

	@Override
	public synchronized void clearSpaceFromServer(ShapeSpace space) {
		this.clearSpace(space);
	}

	@Override
	public void updateClientTime(long gt) {
		this.time = gt;
	}
}
