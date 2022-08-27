package lovexyn0827.mess.log;

import lovexyn0827.mess.util.TickingPhase;
import net.minecraft.entity.Entity;

public interface EntityLogColumn {
	boolean canGetFrom(Entity e);
	TickingPhase getPhase();
	String getName();
	Object getFrom(Entity entity);
}
