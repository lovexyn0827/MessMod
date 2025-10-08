package lovexyn0827.mess.fakes;

import lovexyn0827.mess.util.NoChunkLoadingWorld;
import lovexyn0827.mess.util.PulseRecorder;

public interface ServerWorldInterface {
	PulseRecorder getPulseRecorder();
	NoChunkLoadingWorld toNoChunkLoadingWorld();
}
