package lovexyn0827.mess.network;

import net.minecraft.util.Identifier;

public interface Channels {
	public static int CHANNEL_VERSION = 8;	// TODO Remember to update the channel version if necessary
	Identifier SHAPE = Identifier.of("messmod", "shape");
	Identifier HUD = Identifier.of("messmod", "hud");
	Identifier VERSION = Identifier.of("messmod", "version");
	Identifier UNDO = Identifier.of("messmod", "undo");
	Identifier REDO = Identifier.of("messmod", "redo");
	Identifier OPTIONS = Identifier.of("messmod", "options");
	Identifier OPTION_SINGLE = Identifier.of("messmod", "option_single");
	Identifier ENTITY_DUMP = Identifier.of("messmod", "entity_dump");
}
