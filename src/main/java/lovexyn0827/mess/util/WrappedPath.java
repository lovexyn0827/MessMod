package lovexyn0827.mess.util;

import org.jetbrains.annotations.NotNull;

import lovexyn0827.mess.log.EntityLogColumn;
import lovexyn0827.mess.rendering.hud.data.HudLine;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;

public class WrappedPath implements HudLine {
	protected final AccessingPath path;
	protected final String name;

	public WrappedPath(AccessingPath path, String name) {
		this.path = path;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		WrappedPath other = (WrappedPath) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		
		if (this.path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!this.path.equals(other.path)) {
			return false;
		}
		
		return true;
	}

	@Override
	public @NotNull String getFrom(Entity in) {
		try {
			Object ob = this.path.access(in, in.getClass());
			return ob != null ? ob.toString() : "null";
		} catch (AccessingFailureException e) {
			return e.failureCause.name();
		}
	}

	@Override
	public boolean canGetFrom(Entity entity) {
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", this.name, this.path);
	}

	public static class Phased extends WrappedPath implements EntityLogColumn {
		private final TickingPhase phase;

		public Phased(AccessingPath path, String name, TickingPhase phase) {
			super(path, name);
			this.phase = phase;
		}

		@Override
		public TickingPhase getPhase() {
			return this.phase;
		}
		
		@Override
		public String toString() {
			return String.format("%s(%s@%s)", this.name, this.path, this.phase);
		}
	}
}
