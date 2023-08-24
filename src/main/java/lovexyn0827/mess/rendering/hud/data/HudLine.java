package lovexyn0827.mess.rendering.hud.data;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.Entity;

public interface HudLine{
	@NotNull
	String getFrom(Entity in);
	boolean canGetFrom(Entity entity);
	String getName();
	
	/**
	 * Used on remote clients, to represent a custom field.
	 */
	public static final class Unknown implements HudLine, Comparable<HudLine> {
		public final String name;
		
		Unknown(String name) {
			Objects.requireNonNull(name);
			this.name = name;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			} else {
				if(obj.getClass() == Unknown.class) {
					Unknown other = (Unknown) obj;
					return this.name.equals(other.name);
				} else {
					return false;
				}
			}
		}
		
		@Override
		public @NotNull String getFrom(Entity in) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean canGetFrom(Entity entity) {
			return false;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(HudLine o) {
			if(o instanceof BuiltinHudInfo) {
				return 1;
			} else if(o instanceof Unknown) {
				return this.name.compareTo(((Unknown) o).name);
			}
			
			return 0;
		}
	}
}
