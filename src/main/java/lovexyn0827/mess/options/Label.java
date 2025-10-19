package lovexyn0827.mess.options;

import lovexyn0827.mess.util.i18n.I18N;

public enum Label {
	HIGHLIGHT, 
	RENDERER, 
	INTERACTION_TWEAKS, 
	RESEARCH, 
	REDSTONE, 
	ENTITY, 
	MESSMOD, 
	EXPLOSION, 
	CHUNK, 
	BUGFIX, 
	BREAKING_OPTIMIZATION, 
	MISC;

	public String getReadableName() {
		return I18N.translate(String.format("opt.label.%s.name", this.name()));
	}

	public String getDescription() {
		return I18N.translate(String.format("opt.label.%s.desc", this.name()));
	}
}
