package lovexyn0827.mess.util.blame;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import lovexyn0827.mess.options.OptionManager;

public final class StackTrace {
	private final LinkedList<TraceElement> lines;
	
	public StackTrace(StackTraceElement[] elements) {
		this.lines = new LinkedList<>();
		for(StackTraceElement e : elements) {
			this.lines.add(TraceElement.from(e));
		}
	}
	
	private StackTrace(TraceElement[] elements) {
		this.lines = new LinkedList<>(Lists.newArrayList(elements));
	}
	
	public static StackTrace current() {
		return new StackTrace(Thread.currentThread().getStackTrace()).trim();
	}
	
	private StackTrace trim() {
		this.lines.removeLast();
		return this;
	}
	
	@Nullable
	public Cause blame() {
		switch(OptionManager.blamingMode) {
		case DISABLED:
			return null;
		case SIMPLE_TRACE:
			return new StackTraceCause(this, false);
		case DEOBFUSCATED_TRACE:
			return new StackTraceCause(this.mapToNamed(), true);
		case ANALYZED:
			Set<AnalyzedCause.Clue> clues = new HashSet<>();
			this.lines.forEach((l) -> clues.addAll(l.blame()));
			return new AnalyzedCause(clues);
		default:
			throw new IllegalStateException("Unrecognized blaming mode");
		}
	}
	
	@Nullable
	public static Cause blameCurrent() {
		switch(OptionManager.blamingMode) {
		case DISABLED:
			return null;
		case SIMPLE_TRACE:
			return new StackTraceCause(current().trim(), false);
		case DEOBFUSCATED_TRACE:
			return new StackTraceCause(current().trim(), true);
		case ANALYZED:
			Set<AnalyzedCause.Clue> clues = new HashSet<>();
			current().trim().lines.forEach((l) -> clues.addAll(l.blame()));
			return new AnalyzedCause(clues);
		default:
			throw new IllegalStateException("Unrecognized blaming mode");
		}
	}

	StackTrace mapToNamed() {
		return new StackTrace(this.lines.stream()
				.map(TraceElement::mapToNamed)
				.<TraceElement>toArray((i) -> new TraceElement[i]));
				
	}

	public String toString(char delimiter) {
		StringBuilder sb = new StringBuilder();
		for(TraceElement e : this.lines) {
			sb.append(e.className)
					.append('.')
					.append(e.methodName)
					.append('@')
					.append(e.lineNum < 0 ? "?" : Integer.toString(e.lineNum))
					.append(delimiter);
		}
		
		return sb.substring(0, Math.max(0, sb.length() - 2));
	}
	
	@Override
	public String toString() {
		return this.toString('\n');
	}
}
