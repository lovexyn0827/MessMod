package lovexyn0827.mess.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public abstract class ShapeCache {
	protected final Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> backend = Maps.newHashMap();
	protected long time;
	
	ShapeCache() {
		this.backend.put(World.OVERWORLD, new HashMap<>());
		this.backend.put(World.NETHER, new HashMap<>());
		this.backend.put(World.END, new HashMap<>());
	}
	
	public synchronized Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> getAllShapes() {
		return this.backend;
	}
	
	public synchronized Map<ShapeSpace, Set<Shape>> getShapesInDimension(RegistryKey<World> dimensionType) {
		return this.backend.get(dimensionType);
	}
	
	public synchronized void reset() {
		this.getAllShapes().values().forEach(Map::clear);
	}
	
	public synchronized void clearSpace(ShapeSpace ss) {
		this.getAllShapes().values().forEach((map) -> map.computeIfAbsent(ss, (ss1) -> Sets.newHashSet()).clear());
	}
	
	public static ShapeCache create(MinecraftClient mc) {
		return MessMod.isDedicatedEnv() ? new RemoteShapeCache() : (ShapeCache) MessMod.INSTANCE.shapeSender;
	}
	
	public long getTime() {
		return this.time;
	}
}
