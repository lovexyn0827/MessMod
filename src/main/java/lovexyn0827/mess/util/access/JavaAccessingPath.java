package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.options.OptionManager;

/**
 * AccessingPath implemented using written java code.
 * @author lovexyn0827
 * @date Jul 16, 2023
 */
class JavaAccessingPath implements AccessingPath {
	private final LinkedList<Node> nodes;
	private boolean initialized;
	private final Map<Object, JavaAccessingPath> initializedSubPaths;
	private final String originalStringRepresentation;
	
	protected JavaAccessingPath(List<Node> nodes, String strRep) {
		this.nodes = new LinkedList<>();
		this.nodes.addAll(nodes);
		this.originalStringRepresentation = strRep;
		this.initializedSubPaths = Collections.synchronizedMap(new WeakHashMap<>());
	}

	private JavaAccessingPath(LinkedList<Node> nodes, String strRep,
			Map<Object, JavaAccessingPath> forkSet) {
		this.nodes = new LinkedList<>();
		this.nodes.addAll(nodes);
		this.originalStringRepresentation = strRep;
		this.initializedSubPaths = forkSet;
	}

	@Override
	public Object access(Object start, Type genericType) throws AccessingFailureException {
		return this.getInitializedCopy(start, genericType).accessInternal(start);
	}
	
	private JavaAccessingPath getInitializedCopy(Object start, Type genericType) throws AccessingFailureException {
		switch(OptionManager.accessingPathInitStrategy) {
		case STANDARD: 
			JavaAccessingPath path = this.initializedSubPaths.get(start);
			if(path == null) {
				path = this.createCopyForInput(start);
				if (!path.initialized) {
					path.initialize(genericType);
				}
				
				this.initializedSubPaths.put(start, path);
				return path;
			} else {
				return path;
			}
		case LEGACY: 
			if (!this.initialized) {
				this.initialize(genericType);
			}
			
			return this;
		case STRICT: 
			this.initialize(genericType);
			return this;
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
				throw AccessingFailureException.create(FailureCause.NULL, n, e);
			} catch (AccessingFailureException e2) {
				if(e2.isRaw()) {
					throw e2.withNode(n);
				} else {
					throw e2;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw AccessingFailureException.createWithArgs(FailureCause.ERROR, n, e1, e1);
			}
		}
		
		return intermediate;
	}
	
	/**
	 * Write a new value to the last node of the path.
	 * @param value The string representation of the new value, in the form of literals.
	 * @throws CommandSyntaxException 
	 */
	@Override
	public void write(Object start, Type genericType, String valueStr) 
			throws AccessingFailureException, CommandSyntaxException {
		this.getInitializedCopy(start, genericType).writeInternal(start, Literal.parse(valueStr));
	}

	private void writeInternal(Object start, Literal<?> value) throws AccessingFailureException {
		Object intermediate = start;
		Iterator<Node> itr = this.nodes.iterator();
		Node last = this.nodes.getLast();
		if(!last.isWrittable()) {
			throw AccessingFailureException.create(FailureCause.NOT_WRITTABLE, last);
		}
		
		while(itr.hasNext()) {
			Node n = itr.next();
			if(n == last) {
				break;
			}
			
			try {
				intermediate = n.access(intermediate);
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw AccessingFailureException.create(FailureCause.NULL, n, e);
			} catch (AccessingFailureException e2) {
				if(e2.isRaw()) {
					throw e2.withNode(n);
				} else {
					throw e2;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw AccessingFailureException.createWithArgs(FailureCause.ERROR, n, e1, e1);
			}
		}
		
		try {
			last.write(intermediate, value.get(last.outputType));
		} catch (InvalidLiteralException e) {
			throw AccessingFailureException.create(e, null);
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
		
		if (obj == null || JavaAccessingPath.class != obj.getClass()) {
			return false;
		}
		
		JavaAccessingPath other = (JavaAccessingPath) obj;
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
					if(e.isRaw()) {
						throw e.withNode(n);
					} else {
						throw e;
					}
				}
			}
			
			this.initialized = true;
		}
	}
	
	@Nullable
	@Override
	public Type getOutputType() {
		return this.nodes.getLast().outputType;
	}
	
	private JavaAccessingPath createCopyForInput(Object in) {
		LinkedList<Node> newNodes = new LinkedList<>();
		this.nodes.stream().map((n) -> n.createCopyForInput(in)).forEach(newNodes::add);
		return new JavaAccessingPath(newNodes, this.originalStringRepresentation, this.initializedSubPaths);
	}

	@Override
	public Class<?> compile(List<Class<?>> nodeInputTypes, String name) throws CompilationException {
		return new PathCompiler(this.nodes, nodeInputTypes, name).compile();
	}

	@Override
	public String getOriginalStringRepresentation() {
		return this.originalStringRepresentation;
	}
}
