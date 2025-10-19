package lovexyn0827.mess.util;

import java.lang.reflect.Field;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.entity.EntityLogColumn;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudLine;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;

public class ListenedField implements HudLine, Comparable<HudLine> {
	protected final Field field;
	// XXX Should the name influence the equality?
	protected final String name;
	protected final AccessingPath path;
	//It should be managed by the users.
	//private final TickingPhase phase;
	
	public ListenedField(Field field, AccessingPath path, String customName) {
		this.field = field;
		this.name = customName != null ? customName : MessMod.INSTANCE.getMapping().namedField(this.field.getName());
		this.path = path != null ? path : AccessingPath.DUMMY;
		//this.phase = TickingPhase.TICKED_ALL_WORLDS;
	}

	public boolean canGetFrom(Entity entity) {
		return Reflection.hasField(entity.getClass(), this.field);
	}
	
	public String get(Entity entity) {
		Object ob;
		try {
			this.field.setAccessible(true);
			ob = this.field.get(entity);
			Object result = this.path.access(ob, this.field.getGenericType());
			return result != null ? result.toString() : "[null]";
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} catch (AccessingFailureException e) {
			return e.getShortenedMsg();
		}
		
	}

	@Override
	public String getFrom(Entity in) {
		return this.get(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj == null) {
			return false;
		}
		
		if(obj.getClass() == ListenedField.class) {
			ListenedField other = (ListenedField) obj;
			return this.field.equals(other.field)
					&& this.path.equals(other.path);
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(HudLine o) {
		if(o instanceof BuiltinHudInfo) {
			return 1;
		} else {
			return this.getName().compareTo(o.getName());
		}
	}
	
	@Override
	public String toString() {
		return this.name + '(' + this.field.getName() + '.' + this.path + ')';
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static class Phased extends ListenedField implements EntityLogColumn {
		public final TickingPhase phase;

		public Phased(Field field, AccessingPath path, String customName, TickingPhase phase) {
			super(field, path, customName);
			this.phase = phase;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			
			if(obj == null) {
				return false;
			}
			
			if(obj instanceof Phased) {
				Phased other = (Phased) obj;
				return this.field.equals(other.field)
						&& this.path.equals(other.path)
						&& this.phase.equals(other.phase);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((path == null) ? 0 : phase.hashCode());
			return result;
		}
		
		@Override
		public String toString() {
			return this.name + '(' + this.field.getName() + '.' + this.path + '@' + this.phase + ')';
		}

		@Override
		public TickingPhase getPhase() {
			return this.phase;
		}
	}
}
