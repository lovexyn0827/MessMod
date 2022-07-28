package lovexyn0827.mess.rendering.hud.data;

import net.minecraft.entity.Entity;

public interface HudLine {
	String getFrom(Entity in);
	boolean canGetFrom(Entity entity);
	String getName();
}
