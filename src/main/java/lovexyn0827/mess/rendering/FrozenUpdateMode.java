package lovexyn0827.mess.rendering;

import lovexyn0827.mess.options.EnumParser;

public enum FrozenUpdateMode {
	/**
	 * Add the stuff normally, and remove them normally in the next server side tick.
	 */
	NORMALLY, 
	/**
	 * Everything remains the same as the first frozen tick.
	 */
	PAUSE,
	/**
	 * The addition works normally, but the removal pauses.
	 */
	NO_REMOVAL;
	
	public static class Parser extends EnumParser<FrozenUpdateMode> {
		public Parser() {
			super(FrozenUpdateMode.class);
		}
	}
}