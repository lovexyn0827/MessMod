package lovexyn0827.mess.rendering.hud.data;

import net.minecraft.entity.Entity;

public interface HudLine {
	String toLine(Object data);
	boolean canGetFrom(Entity entity);
	String getName();
}
