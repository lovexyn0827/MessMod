package lovexyn0827.mess.rendering;

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
			if(obj.getClass() == ShapeSpace.class) {
				ShapeSpace other = (ShapeSpace) obj;
				return this.name.equals(other.name);
			} else {
				return false;
			}
		}
	}
}