package lovexyn0827.mess.log.entity;

import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;

public interface EntityLogColumn {
	boolean canGetFrom(Entity e);
	TickingPhase getPhase();
	String getName();
	Object getFrom(Entity entity);
}
