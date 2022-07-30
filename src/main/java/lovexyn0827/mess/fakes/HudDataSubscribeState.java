package lovexyn0827.mess.fakes;

import lovexyn0827.mess.rendering.hud.HudType;

public interface HudDataSubscribeState {
	boolean isSubscribed(HudType type);
	void subscribe(HudType type);
	void unsubscribe(HudType type);
}
