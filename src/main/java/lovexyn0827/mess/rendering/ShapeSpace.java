package lovexyn0827.mess.rendering;

import lovexyn0827.mess.rendering.hud.data.HudLine.Unknown;

public class ShapeSpace {
	/**
	 * Default space, which can only be managed by the ShapeRenderer class automatically.
	 */
	public static final ShapeSpace DEFAULT = new ShapeSpace("default");
	public final String name;
	
	public ShapeSpace(String name) {
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
}