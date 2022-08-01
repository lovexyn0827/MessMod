package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.options.EnumParser;
import lovexyn0827.mess.options.OptionManager;

/**
 * A sequence of {@link Node}, used to get some fields or elements from an object.
 * @author lovexyn0827
 * Date: April 22, 2022
 */
public final class AccessingPath {
	public static final AccessingPath DUMMY = new AccessingPath(Collections.emptyList());
	private final LinkedList<Node> nodes;
	private boolean initialized;
	
	@Nullable
	private WeakHashMap<Object, AccessingPath> initializedSubPaths = new WeakHashMap<>();
	
	AccessingPath(List<Node> nodes) {
		this.nodes = new LinkedList<>();
		this.nodes.addAll(nodes);
	}

	public Object access(Object start, Type genericType) throws AccessingFailureException {
		switch(OptionManager.accessingPathInitStrategy) {
		case STANDARD: 
			AccessingPath path = this.initializedSubPaths.get(start);
			if(path == null) {
				path = this.createCopyForInput(start);
				if (!path.initialized) {
					path.initialize(genericType);
				}
				this.initializedSubPaths.put(start, path);
			}
			
			return path.accessInternal(start);
		case LEGACY: 
			if (!this.initialized) {
				this.initialize(genericType);
			}
			
			return this.accessInternal(start);
		case STRICT: 
			this.initialize(genericType);
			Object result = this.accessInternal(start);
			this.uninitialize();
			return result;
		default:
			throw new IllegalStateException();
		}		
	}

	private Object accessInternal(Object start) throws AccessingFailureException {
		Object intermediate = start;
		Iterator<Node> itr = this.nodes.iterator();
		while(itr.hasNext()) {
			Node n = itr.next();
			try {
				intermediate = n.access(intermediate);
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw new AccessingFailureException(AccessingFailureException.Cause.NULL, n, e);
			} catch (AccessingFailureException e2) {
				if(e2.getShortenedMsg() == null) {
					throw new AccessingFailureException(e2.cause, n, e2.getCause(), e2.args);
				} else {
					throw e2;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new AccessingFailureException(AccessingFailureException.Cause.ERROR, n, e1, e1);
			}
		}
		
		return intermediate;
	}

	@Override
	public int hashCode() {
		return this.nodes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || AccessingPath.class != obj.getClass()) {
			return false;
		}
		
		AccessingPath other = (AccessingPath) obj;
		return this.nodes.equals(other.nodes);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.nodes.forEach((n) -> {
			if(n != null) {
				sb.append(n.toString());
			}
			
			sb.append('.');
		});
		return sb.toString();
	}
	
	private void initialize(Type startType) throws AccessingFailureException {
		if (!this.initialized) {
			Type lastType = startType;
			for (Node n : this.nodes) {
				try {
					n.initialize(lastType);
					lastType = n.outputType;
				} catch (AccessingFailureException e) {
					if(e.getShortenedMsg() == null) {
						throw new AccessingFailureException(e.cause, n, e.getCause(), e.args);
					} else {
						throw e;
					}
				}
			}
			
			this.initialized = true;
		}
	}
	
	private void uninitialize() {
		this.initialized = false;
	}
	
	@Nullable
	public Type getOutputType() {
		return this.nodes.getLast().outputType;
	}
	
	private AccessingPath createCopyForInput(Object in) {
		LinkedList<Node> newNodes = new LinkedList<>();
		this.nodes.stream().map((n) -> n.createCopyForInput(in)).forEach(newNodes::add);
		return new AccessingPath(newNodes);
	}
	
	public static enum InitializationStrategy {
		LEGACY, 
		STANDARD, 
		STRICT;
		
		public static class Parser extends EnumParser<InitializationStrategy> {
			public Parser() {
				super(InitializationStrategy.class);
			}
		}
	}
}
