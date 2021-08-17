package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.MessMod;

public class HudUtil {
	public static boolean isLeftAligned() {
		return MessMod.INSTANCE.getOption("alignMode").contains("Left");
	}
}
