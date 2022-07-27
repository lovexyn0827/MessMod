package lovexyn0827.mess.util;

import java.lang.reflect.Field;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.data.BuiltinHudInfo;
import lovexyn0827.mess.rendering.hud.data.HudLine;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;

public class ListenedField implements HudLine, Comparable<HudLine> {
	private final Field field;
	// XXX Should the name influence the equality?
	private final String name;
	private final AccessingPath path;
	
	public ListenedField(Field field, AccessingPath path, String customName) {
		this.field = field;
		this.name = customName != null ? customName : MessMod.INSTANCE.getMapping().namedField(this.field.getName());
		this.path = path != null ? path : AccessingPath.DUMMY;
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

	public String getCustomName() {
		return this.name;
	}

	@Override
	// XXX
	public String toLine(Object data) {
		return this.get(((Entity) data));
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
		
		if(obj instanceof ListenedField) {
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
		} else if(o instanceof ListenedField) {
			return this.name.compareTo(((ListenedField) o).name);
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		return this.name + '(' + this.field.getName() + '.' + this.path + ')';
	}

	@Override
	public String getName() {
		return this.getCustomName();
	}
}
