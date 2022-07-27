package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.util.Reflection;

/**
 * Currently, there are six kinds of Node, they are: 
 * 
 *  - [index]: An element in an array or a collection
 *  
 *  - &lt;key&gt;: The value associated with the given key in a map
 *  
 *  - !field: The field with the given name
 *  
 *  - method(): The method with the given name, arguments are not supported in this version
 *  
 *  - x, y, and z: A component of a vector ({@code Vec3d}, {@code BlockPos}, or {@code ChunkPos}), or the coordination an entity
 *  
 *  - identityHash: The identity hash code of an object, which is usually the 
 *  	same as the one returned by the default {@code hashcode()} method.
 *  
 * @author lovexyn0827
 * Date: April 22, 2022
 */
abstract class Node {
	/**
	 * The type of the output of this node, or {@code null} if the type cannot be determined. Primitive types shouldn't be wrapped.
	 */
	@Nullable
	protected Type outputType;
	private boolean initialized;
	/** Shouldn't be modified twice */
	int ordinary;
	
	boolean canFollow(Node n) {
		return n.outputType != null && !Reflection.isPrimitive(n.outputType);
	}
	
	abstract Object access(Object previous) throws AccessingFailureException;
	
	Set<String> listSuggestions() {
		if(this.outputType != null) {
			
		}
		
		return null;
	}

	boolean isInitialized() {
		return initialized;
	}

	final void initialize(Type lastOutClass) throws AccessingFailureException {
		this.prepare(lastOutClass);
		this.initialized = true;
	}
	
	void uninitialize() {
		this.outputType = null;
		this.initialized = false;
	}

	/**
	 * @throws AccessingFailureException 
	 * @implNote outputType field shouldn't be null after initialization.
	 */
	protected abstract Type prepare(Type lastOutType) throws AccessingFailureException;
	
	/** Nodes obtained this way must maintain its original position in the path */
	Node createCopyForInput(Object input) {
		return this;
	}
}
