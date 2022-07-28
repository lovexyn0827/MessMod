package lovexyn0827.mess.util.access;

import lovexyn0827.mess.util.i18n.I18N;

public class AccessingFailureException extends Exception {
	private static final long serialVersionUID = -4184399838031396060L;
	private final String shortenedMsg;
	final Cause cause;
	final Object[] args;

	AccessingFailureException(Cause cause, Node node, Throwable e, Object ... args) {
		super(I18N.translate(cause.translationKey, args) 
				+ '(' + "Node#" + node.ordinary + ',' + node.toString() + ')' , e);
		this.shortenedMsg = cause.name() + '@' + node.ordinary;
		this.cause = cause;
		this.args = args;
	}

	public AccessingFailureException(Cause cause, Node node, Object ... args) {
		super(I18N.translate(cause.translationKey, args) 
				+ '(' + "Node#" + node.ordinary + ',' + node.toString() + ')');
		this.shortenedMsg = cause.name() + '@' + node.ordinary;
		this.cause = cause;
		this.args = args;
	}
	
	public AccessingFailureException(Cause cause, Object ... args) {
		super();
		this.shortenedMsg = null;
		this.cause = cause;
		this.args = args;
	}
	
	public AccessingFailureException(Cause cause, Throwable e, Object ... args) {
		super(e);
		this.shortenedMsg = null;
		this.cause = cause;
		this.args = args;
	}

	public String getShortenedMsg() {
		return shortenedMsg;
	}
	
	static enum Cause {
		NO_FIELD("exp.nofield"), 
		NO_METHOD("exp.nomethod"), 
		NO_KEY("exp.nokey"), 
		OUT_OF_BOUND("exp.outbound"), 
		NULL("exp.null"), 
		INVOKE_FAIL("exp.failexec"), 
		BAD_INPUT("exp.invalidlast"), 
		UNCERTAIN_CLASS("exp.unboundedclass"), 
		NO_CLASS("exp.noclass"), 
		NOT_MAP("exp.notmap"), 
		BAD_ARG("exp.badarg"), 
		MULTI_TARGET("exp.multitarget"), 
		INV_STATIC("exp.staticl.format"), 
		ERROR("exp.unexc");
		
		final String translationKey;
		
		Cause(String translationKey) {
			this.translationKey = translationKey;
			
		}
	}
}
