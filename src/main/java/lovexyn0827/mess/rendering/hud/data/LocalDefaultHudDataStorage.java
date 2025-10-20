package lovexyn0827.mess.rendering.hud.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LocalDefaultHudDataStorage extends LocalDataStorage {
	public LocalDefaultHudDataStorage() {
		for (HudLine l : BuiltinHudInfo.values()) {
			this.addCustomLine(l);
		}
	}
}
