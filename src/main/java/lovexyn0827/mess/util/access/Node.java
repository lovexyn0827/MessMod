package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.util.Reflection;

/**
 * Currently, there are 10 kinds of Node, they are: 
 * 
 * <pre>
 *  - [index]: An element in an array or a collection
 *  - &lt;key&gt;: The value associated with the given key in a map
 *  - !field: The field with the given name
 *  - method(): The method with the given name, arguments are not supported in this version
 *  - x, y, and z: A component of a vector ({@code Vec3d}, {@code BlockPos}, or {@code ChunkPos}), 
 *      or the coordination an entity
 *  - identityHash: The identity hash code of an object, which is usually the 
 *  	same as the one returned by the default {@code hashcode()} method.
 *  - size: The size of a list, map, array, etc.
 *  - regular custom node: A named accessing path defined by the user.
 *  - >Class::method: Mapper node, a more generic form of method node which supports using the 
 *      previous input as an argument of the given method.
 *  - (Class): Cast the input to a given type.
 * </pre>
 * @author lovexyn0827
 * Date: April 22, 2022
 */
abstract class Node {
	/**
	 * The type of the output of this node, or {@code null} if the type cannot be determined. Primitive types 
	 * shouldn't be wrapped.
	 */
	@Nullable
	protected Type outputType;
	private boolean initialized;
	/** Shouldn't be modified twice */
	int ordinary;
	protected Type inputType;
	
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
		return this.initialized;
	}

	/**
	 * Should be called after custom initialization finished.
	 * @param lastOutClass
	 * @throws AccessingFailureException
	 * @implNote outputType field shouldn't be null after initialization.
	 */
	void initialize(Type lastOutClass) throws AccessingFailureException {
		try {
			this.outputType =  this.resolveOutputType(lastOutClass);
		} catch (InvalidLiteralException e) {
			throw AccessingFailureException.create(e, this);
		}
		
		this.initialized = true;
		this.inputType = lastOutClass;
	}
	
	void uninitialize() {
		this.outputType = null;
		this.initialized = false;
	}

	/**
	 * @throws AccessingFailureException 
	 * @throws InvalidLiteralException 
	 * @implNote Never modify any field of this {@code Node}
	 */
	protected abstract Type resolveOutputType(Type lastOutType) 
			throws AccessingFailureException, InvalidLiteralException;
	
	/** Nodes obtained this way must maintain its original position in the path */
	Node createCopyForInput(Object input) {
		return this;
	}

	boolean isWrittable() {
		return false;
	}
	
	void write(Object writeTo, Object newValue) throws AccessingFailureException {
		throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, this);
	}

	protected void ensureInitialized() {
		if(!this.isInitialized()) {
			throw new IllegalStateException("Called before initialization!");
		}
	}
	
	abstract NodeCompiler getCompiler();
	
	boolean allowsPrimitiveTypes() {
		return false;
	}
}
