package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.options.OptionManager;

public class HudUtil {
	public static boolean isLeftAligned() {
		return OptionManager.hudAlignMode.name().contains("LEFT");
	}
}
