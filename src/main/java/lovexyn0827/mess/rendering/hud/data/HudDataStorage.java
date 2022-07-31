package lovexyn0827.mess.rendering.hud.data;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.rendering.hud.ServerHudManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A client-side cache of the data in HUDs
 * @author lovexyn0827
 * @date 2022/7/14
 */
@Environment(EnvType.CLIENT)
public interface HudDataStorage {
	int size();
	Object get(HudLine id);
	Iterator<Entry<HudLine, Object>> iterator();
	default void forEach(BiConsumer<String, Object> action) {
		synchronized (this) {
			this.iterator().forEachRemaining((entry) -> action.accept(entry.getKey().getName(), entry.getValue()));
		}
	}
	
	static HudDataStorage create(HudType type) {
		if(MessMod.isDedicatedEnv()) {
			return new RemoteHudDataStorage();
		} else {
			ServerHudManager shm = MessMod.INSTANCE.getServerHudManager();
			switch(type) {
			case TARGET : 
				return (HudDataStorage) shm.lookingHud;
			case SERVER_PLAYER : 
				return (HudDataStorage) shm.playerHudS;
			case CLIENT_PLAYER : 
				return (HudDataStorage) shm.playerHudC;
			case SIDEBAR : 
				return (HudDataStorage) shm.sidebar;
			}
			
			throw new AssertionError();
		}
	}
}
