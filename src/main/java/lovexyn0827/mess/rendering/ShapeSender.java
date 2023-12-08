package lovexyn0827.mess.rendering;

import lovexyn0827.mess.MessMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface ShapeSender {
	void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space, ServerPlayerEntity player);
	void updateClientTime(long gt);
	default void addShape(Shape shape, RegistryKey<World> dim, ServerPlayerEntity player) {
		this.addShape(shape, dim, ShapeSpace.DEFAULT, player);
	}
	
	void clearSpaceFromServer(ShapeSpace space, ServerPlayerEntity e);
	
	static ShapeSender create(MinecraftServer server) {
		return MessMod.isDedicatedEnv() ? new RemoteShapeSender(server) : new LocalShapeStorage();
	}
}
