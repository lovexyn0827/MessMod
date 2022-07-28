package lovexyn0827.mess.rendering.hud.data;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.rendering.hud.ServerHudManager;

/**
 * A client-side cache of the data in HUDs
 * @author lovexyn0827
 * @date 2022/7/14
 */
public interface HudDataStorage {
	void pushData(String name, Object data);
	int size();
	Object get(HudLine id);
	Iterator<Entry<HudLine, Object>> iterator();
	default void forEach(BiConsumer<String, Object> action) {
		this.iterator().forEachRemaining((entry) -> action.accept(entry.getKey().getName(), entry.getKey()));
	}
	
	static HudDataStorage create(HudType type) {
		if(MessMod.INSTANCE.isDedicatedEnv()) {
			// TODO
			throw new AssertionError();
		} else {
			ServerHudManager shm = MessMod.INSTANCE.getServerHudManager();
			switch(type) {
			case TARGET: 
				return (HudDataStorage) shm.lookingHud;
			case SERVER_PLAYER: 
				return (HudDataStorage) shm.playerHudS;
			case CLIENT_PLAYER: 
				return (HudDataStorage) shm.playerHudC;
			}
			
			throw new AssertionError();
		}
	}
}
