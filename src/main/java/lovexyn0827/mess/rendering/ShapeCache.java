package lovexyn0827.mess.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class ShapeCache {
	protected final Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> backend = Maps.newHashMap();
	protected long time;
	
	public final synchronized Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> getAllShapes() {
		return this.backend;
	}
	
	public final synchronized Map<ShapeSpace, Set<Shape>> getShapesInDimension(RegistryKey<World> dimensionType) {
		return this.backend.computeIfAbsent(dimensionType, (k) -> new HashMap<>());
	}
	
	public final synchronized void reset() {
		this.backend.values().forEach(Map::clear);
	}
	
	public final synchronized void clearSpace(ShapeSpace ss) {
		this.backend.values().forEach((map) -> map.computeIfAbsent(ss, (ss1) -> Sets.newConcurrentHashSet()).clear());
	}
	
	public static ShapeCache create(MinecraftClient mc) {
		return MessMod.isDedicatedEnv() ? new RemoteShapeCache() : (ShapeCache) MessMod.INSTANCE.shapeSender;
	}
	
	public final long getTime() {
		return this.time;
	}

	public void close() {
	}
}
