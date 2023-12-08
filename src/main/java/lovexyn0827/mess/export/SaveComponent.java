package lovexyn0827.mess.export;

import com.google.common.collect.ImmutableBiMap;

import lovexyn0827.mess.options.ListParser;

public enum SaveComponent {
	REGION, 
	POI, 
	ENTITY, 
	GAMERULES, 
	RAID, 
	MAP_LOCAL, 
	MAP_OTHER,
	ICON, 
	ADVANCEMENTS_SELF, 
	ADVANCEMENT_OTHER, 
	PLAYER_SELF, 
	PLAYER_OTHER, 
	STAT_SELF, 
	STAT_OTHER, 
	SCOREBOARD, 
	FORCE_CHUNKS_LOCAL, 
	FORCE_CHUNKS_OTHER, 
	DATA_COMMAND_STORAGE, 
	CARPET, 
	MESSMOD;
	
	private static final ImmutableBiMap<String, SaveComponent> BY_NAME;
	
	static {
		ImmutableBiMap.Builder<String, SaveComponent> builder = ImmutableBiMap.builder();
		for(SaveComponent comp : values()) {
			builder.put(comp.name(), comp);
		}
		
		BY_NAME = builder.build();
	}
	
	public static class DefaultListParser extends ListParser<SaveComponent> {
		public DefaultListParser() {
			super(BY_NAME);
		}
	}
}
