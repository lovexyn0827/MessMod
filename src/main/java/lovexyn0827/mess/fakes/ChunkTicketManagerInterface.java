package lovexyn0827.mess.fakes;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public interface ChunkTicketManagerInterface {
	Identifier getDimesionId();
	void initWorld(ServerWorld world);
}
