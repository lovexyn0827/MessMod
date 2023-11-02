package lovexyn0827.mess.network;

import net.minecraft.util.Identifier;

public interface Channels {
	public static int CHANNEL_VERSION = 5;	// TODO Remember to update the channel version if necessary
	Identifier SHAPE = new Identifier("messmod", "shape");
	Identifier HUD = new Identifier("messmod", "hud");
	Identifier VERSION = new Identifier("messmod", "version");
	Identifier UNDO = new Identifier("messmod", "undo");
	Identifier REDO = new Identifier("messmod", "redo");
	Identifier OPTIONS = new Identifier("messmod", "options");
	Identifier ENTITY_DUMP = new Identifier("messmod", "entity_dump");
}
