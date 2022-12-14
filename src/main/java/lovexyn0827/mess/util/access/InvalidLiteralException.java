package lovexyn0827.mess.util.access;

import lovexyn0827.mess.util.i18n.I18N;

public class InvalidLiteralException extends Exception {
	private static final long serialVersionUID = -4184399838031396061L;
	private final String shortenedMsg;
	public final FailureCause failureCause;
	final Object[] args;
	
	/**
	 * Whether or not the node from which the exception arose is specified.
	 */
	private boolean raw;

	private InvalidLiteralException(FailureCause failureCause, Literal<?> literal, Throwable e, Object ... args) {
		super(I18N.translate(failureCause.translationKey, args) 
				+ '(' + "Node#" + '?' + ',' + literal.toString() + ')' , e);
		this.shortenedMsg = failureCause.name() + '@' + '?';
		this.failureCause = failureCause;
		this.args = args;
		this.raw = false;
	}
	
	static InvalidLiteralException create(FailureCause failureCause, Literal<?> literal) {
		return new InvalidLiteralException(failureCause, literal, null);
	}
	
	static InvalidLiteralException create(FailureCause failureCause, Literal<?> literal, Throwable e) {
		return new InvalidLiteralException(failureCause, literal, e);
	}
	
	static InvalidLiteralException createWithArgs(FailureCause failureCause, Literal<?> literal, Throwable e, Object ... args) {
		return new InvalidLiteralException(failureCause, literal, e, args);
	}
	
	@Override
	public String getMessage() {
		return shortenedMsg;
	}

	public String getShortenedMsg() {
		return this.shortenedMsg;
	}

	public boolean isRaw() {
		return this.raw;
	}
	
	public AccessingFailureException withNode(Node node) {
		return AccessingFailureException.createWithArgs(failureCause, node, this.getCause(), args);
	}
}
