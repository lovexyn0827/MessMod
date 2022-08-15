package lovexyn0827.mess.network;

import net.minecraft.util.Identifier;

public interface Channels {
	public static int CHANNEL_VERSION = 1;	// TODO Remember to update the channel version if necessary
	Identifier SHAPE = new Identifier("messmod", "shape");
	Identifier HUD = new Identifier("messmod", "hud");
	Identifier VERSION = new Identifier("messmod", "version");
	Identifier UNDO = new Identifier("messmod", "undo");
	Identifier REDO = new Identifier("messmod", "redo");
}
