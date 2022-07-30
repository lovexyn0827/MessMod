package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface ShapeSender {
	void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space);
	void updateClientTime(long gt);
	default void addShape(Shape shape, RegistryKey<World> dim) {
		this.addShape(shape, dim, ShapeSpace.DEFAULT);
	}
	
	void clearSpaceFromServer(ShapeSpace space);
	
	static ShapeSender create(MinecraftServer server) {
		return MessMod.isDedicatedEnv() ? new RemoteShapeSender(server) : new LocalShapeStorage();
	}
}
