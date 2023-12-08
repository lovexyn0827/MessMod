package lovexyn0827.mess.util;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.phase.ServerTickingPhase;

public class ServerMicroTime {
	public final long gameTime;
	public final ServerTickingPhase phase;
	
	public ServerMicroTime(long gameTime, ServerTickingPhase phase) {
		this.gameTime = gameTime;
		this.phase = phase;
	}
	
	public static ServerMicroTime current() {
		return new ServerMicroTime(MessMod.INSTANCE.getGameTime(), ServerTickingPhase.current());
	}
}
