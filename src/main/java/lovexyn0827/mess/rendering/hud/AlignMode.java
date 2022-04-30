package lovexyn0827.mess.rendering.hud;

import lovexyn0827.mess.options.EnumParser;

public enum AlignMode {
	TOP_LEFT,
	TOP_RIGHT,
	BOTTIM_LEFT,
	BOTTOM_RIGHT;
	
	public static class Parser extends EnumParser<AlignMode> {
		public Parser() {
			super(AlignMode.class);
		}
	}
}
