package lovexyn0827.mess.util.blame;

import lovexyn0827.mess.options.EnumParser;

public enum Confidence implements Comparable<Confidence> {
	IMPOSSIBLE, 
	/**
	 * Nearly impossible, but unable to prove.
	 */
	UNLIKELY, 
	POSSIBLE, 
	PROBABLE, 
	DEFINITE;

	public boolean isAtLeast(Confidence other) {
		return this.ordinal() >= other.ordinal();
	}
	
	public static class Parser extends EnumParser<Confidence> {
		public Parser() {
			super(Confidence.class);
		}
	}
}
