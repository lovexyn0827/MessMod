package lovexyn0827.mess.util;

import lovexyn0827.mess.util.i18n.I18N;

@SuppressWarnings("serial")
public class TranslatableException extends RuntimeException {

	/**
	 * The message is translated by default.
	 * @param msg
	 */
	public TranslatableException(String msg) {
		super(I18N.translate(msg));
	}
	
	public TranslatableException(String msg, Object ... args) {
		super(String.format(I18N.translate(msg), args));
	}

}
