package lovexyn0827.mess.util.access;

import lovexyn0827.mess.util.i18n.I18N;

public class CompilationException extends Exception {
	public CompilationException(String key, Object ... args) {
		super(I18N.translate(key, args));
	}
	
	public CompilationException(FailureCause cause, Object ... args) {
		super(I18N.translate(cause.translationKey, args));
	}

	private static final long serialVersionUID = 202306280958L;
	
}
