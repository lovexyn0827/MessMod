package lovexyn0827.mess.util.access;

import lovexyn0827.mess.util.i18n.I18N;

public class AccessingFailureException extends Exception {
	private static final long serialVersionUID = -4184399838031396060L;
	private final String shortenedMsg;
	public final FailureCause failureCause;
	final Object[] args;
	
	/**
	 * Whether or not the node from which the exception arose is specified.
	 */
	private boolean raw;

	private AccessingFailureException(FailureCause failureCause, Node node, Throwable e, Object ... args) {
		super(I18N.translate(failureCause.translationKey, args) 
				+ '(' + "Node#" + (node == null ? '?' : node.ordinary) + ','
				+ (node == null ? '?' : node) + ')' , e);
		this.shortenedMsg = failureCause.name() + '@' + (node == null ? '?' : node.ordinary);
		this.failureCause = failureCause;
		this.args = args;
		this.raw = false;
	}
	
	static AccessingFailureException create(FailureCause failureCause, Node node) {
		return new AccessingFailureException(failureCause, node, null);
	}
	
	static AccessingFailureException create(InvalidLiteralException e, Node node) {
		return new AccessingFailureException(e.failureCause, node, e.getCause(), e.args);
	}
	
	static AccessingFailureException create(FailureCause failureCause, Node node, Throwable e) {
		return new AccessingFailureException(failureCause, node, e);
	}
	
	static AccessingFailureException createWithArgs(FailureCause failureCause, Node node, Throwable e, Object ... args) {
		return new AccessingFailureException(failureCause, node, e, args);
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
