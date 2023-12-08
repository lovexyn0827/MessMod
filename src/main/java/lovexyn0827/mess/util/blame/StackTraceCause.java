package lovexyn0827.mess.util.blame;

public class StackTraceCause implements Cause {
	private StackTrace trace;

	public StackTraceCause(StackTrace stackTrace, boolean map) {
		this.trace = map ? stackTrace.mapToNamed() : stackTrace;
	}

	@Override 
	public String toString() {
		return this.trace.toString('|');
	}
}
