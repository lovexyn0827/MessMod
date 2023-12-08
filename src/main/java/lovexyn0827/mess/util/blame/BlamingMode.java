package lovexyn0827.mess.util.blame;

import lovexyn0827.mess.options.EnumParser;

public enum BlamingMode {
	DISABLED, 
	SIMPLE_TRACE, 
	DEOBFUSCATED_TRACE, 
	ANALYZED;
	
	public static class Parser extends EnumParser<BlamingMode> {
		public Parser() {
			super(BlamingMode.class);
		}
	}
}
