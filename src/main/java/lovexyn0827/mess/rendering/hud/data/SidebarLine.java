package lovexyn0827.mess.rendering.hud.data;

import org.jetbrains.annotations.NotNull;

import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;

public final class SidebarLine implements HudLine, Comparable<HudLine> {
	private final HudLine backend;
	public final Entity entity;
	public final TickingPhase updatePhase;
	
	public SidebarLine(HudLine backend, Entity e, TickingPhase updatePhase) {
		this.backend = backend;
		this.entity = e;
		this.updatePhase = updatePhase;
	}

	@Override
	public @NotNull String getFrom(Entity in) {
		if(in != this.entity) {
			throw new IllegalArgumentException();
		}
		
		return this.backend.getFrom(in);
	}

	@Override
	public boolean canGetFrom(Entity entity) {
		return entity == this.entity && this.backend.canGetFrom(entity);
	}
	
	public Object get() {
		return this.backend.getFrom(this.entity);
	}

	@Override
	public String getName() {
		return this.backend.getName();
	}

	@Override
	public int compareTo(HudLine o) {
		return this.getName().compareTo(o.getName());
	}

	public boolean canGet() {
		return this.backend.canGetFrom(this.entity);
	}

}
