package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

/**
 * A sequence of {@link Node}, used to get some fields or elements from an object.
 * @author lovexyn0827
 * Date: April 22, 2022
 */
public final class AccessingPath {
	public static final AccessingPath DUMMY = new AccessingPath(Collections.emptyList());
	private final LinkedList<Node> nodes;
	private boolean initialized;
	
	AccessingPath(List<Node> nodes) {
		this.nodes = new LinkedList<>();
		this.nodes.addAll(nodes);
	}

	public Object access(Object start) {
		if (!this.initialized) {
			throw new IllegalStateException("This accessing path hasn't been initialized yet!");
		}
		
		try {
			MutableObject<Object> mo = new MutableObject<>(start);
			this.nodes.forEach((node) -> mo.setValue(node.access(mo.getValue())));
			return mo.getValue();
		} catch (NullPointerException | NoSuchElementException e) {
			return null;
		}
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
	
	public void initialize(Type startType) {
		if (!this.initialized) {
			Type lastType = startType;
			for (Node n : this.nodes) {
				n.initialize(lastType);
				lastType = n.outputType;
			} 
			
			this.initialized = true;
		}
	}
	
	@Nullable
	public Type getOutputType() {
		return this.nodes.getLast().outputType;
	}
}
