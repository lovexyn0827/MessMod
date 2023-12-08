package lovexyn0827.mess.util.access;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import lovexyn0827.mess.util.LockableLinkedHashMap;
import lovexyn0827.mess.util.LockableList;
import lovexyn0827.mess.util.Reflection;

class CompilationContext {
	private static final AtomicInteger NEXT_PATH_ID = new AtomicInteger(0);
	private final LockableList<AccessingPath> subPaths = LockableList.create();
	private final LockableList<Pair<Member, CallableType>> callables = LockableList.create();
	private final LockableList<Literal<?>> dynamicLiterals = LockableList.create();
	private final LockableLinkedHashMap<Class<?>, Pair<Integer, List<Literal<?>>>> staticLiterals = 
			new LockableLinkedHashMap<>();
	private final LockableList<Function<?, ?>> lambdas = LockableList.create();
	private Type lastOutputType;
	private int maxLocal = 3;	// Slot 0 is reserved for 'this' object, while 1 & 2 are for arguments.
	private String internalName = allocateClassName();
	private final List<Class<?>> nodeInputTypeOverride;
	private int currentNodeOrdinal = 0;
	private final String name;
	
	public CompilationContext(List<Class<?>> nodeInputTypes, String name) {
		this.nodeInputTypeOverride = nodeInputTypes;
		this.name = name;
		if(nodeInputTypes.isEmpty()) {
			// The path being compiled has no nodes, thus it can accept all types of input objects
			this.lastOutputType = Object.class;
		} else {
			this.lastOutputType = nodeInputTypes.get(0);
		}
	}

	public int allocateSubPath(AccessingPath path) {
		this.subPaths.add(path);
		return this.subPaths.size() - 1;
	}

	public Type getLastOutputType() {
		return this.lastOutputType;
	}

	public int allocateMethod(Method method) throws CompilationException {
		this.callables.add(new Pair<>(method, CallableType.INVOKER));
		return this.callables.size() - 1;
	}

	public int allocateFieldGetter(Field field) throws CompilationException {
		this.callables.add(new Pair<>(field, CallableType.GETTER));
		return this.callables.size() - 1;
	}
	
	public int allocateFieldSetter(Field field) throws CompilationException {
		this.callables.add(new Pair<>(field, CallableType.SETTER));
		return this.callables.size() - 1;
	}

	public int allocateLambda(Function<?, ?> func) throws CompilationException {
		if(!(func instanceof Serializable)) {
			throw new IllegalArgumentException("func should be an instance of Serializable");
		}
		
		this.lambdas.add(func);
		return this.lambdas.size() - 1;
	}
	
	public int allocateDynamicLiteral(Literal<?> literal) {
		this.dynamicLiterals.add(literal);
		return this.dynamicLiterals.size() - 1;
	}
	
	public Pair<Integer, Integer> allocateStaticLiteral(Literal<?> literal) throws CompilationException {
		if(!literal.isStatic()) {
			throw new CompilationException(FailureCause.ERROR, "Trying to add dynamic constants.");
		}
		
		Object cst;
		try {
			cst = literal.get(null);
		} catch (InvalidLiteralException e) {
			throw new CompilationException(FailureCause.ERROR, e.getMessage());
		}
		
		Pair<Integer, List<Literal<?>>> listWraper = this.staticLiterals.computeIfAbsent(cst.getClass(), (c) -> {
			return new Pair<>(this.staticLiterals.size(), new ArrayList<>());
		});
		Pair<Integer, Integer> position = new Pair<>(listWraper.getFirst(), listWraper.getSecond().size());
		listWraper.getSecond().add(literal);
		return position;
	}
	
	public Class<?> getLastOutputClass() {
		if(this.nodeInputTypeOverride.size() > this.currentNodeOrdinal) {
			Class<?> override = this.nodeInputTypeOverride.get(this.currentNodeOrdinal);
			if(override != null) {
				return override;
			}
		}
		
		return Reflection.getRawType(this.lastOutputType);
	}
	
	/**
	 * Signal the completion of the compilation process of a node, and update the output type of the last node.
	 * The method should be called only once when finishing the compilation of a node.
	 * @param outputType
	 */
	public void endNode(Type outputType) {
		this.lastOutputType = outputType;
		this.currentNodeOrdinal++;
	}

	public int allocateLocalVar() {
		return this.maxLocal++;
	}
	
	public int allocateWideLocalVar() {
		return this.maxLocal += 2;
	}

	public String getInternalClassNameOfPath() {
		return this.internalName;
	}
	
	public void lockConstantLists() {
		this.callables.lock();
		this.subPaths.lock();
		this.dynamicLiterals.lock();
		this.staticLiterals.lock();
		this.lambdas.lock();
	}

	public List<AccessingPath> getSubPaths() {
		return this.subPaths;
	}

	public List<Literal<?>> getDynamicLiterals() {
		return this.dynamicLiterals;
	}

	public LinkedHashMap<Class<?>, Pair<Integer, List<Literal<?>>>> getStaticLiterals() {
		return this.staticLiterals;
	}
	
	public List<Function<?,?>> getLambdas() {
		return this.lambdas;
	}

//	public int allocateReflectedField(Field field) {
//		this.fields.add(func);
//		return this.functions.size() - 1;
//	}
//
//	public int allocateReflectedMethod() {
//		this.functions.add(func);
//		return this.functions.size() - 1;
//	}

	public String getOriginalName() {
		return this.name;
	}
	
	private static String allocateClassName() {
		String name;
		do {
			name = "lovexyn0827/mess/util/access/CompiledPath_" + NEXT_PATH_ID.getAndIncrement();
		} while(Reflection.isClassExisting(name));
		
		return name;
	}

	public Class<?> getInputClassOverrideAt(int i) {
		if(i == 0 && this.nodeInputTypeOverride.size() == 0) {
			return Object.class;
		}
		
		return this.nodeInputTypeOverride.get(i);
	}
	
	static enum CallableType {
		INVOKER, 
		GETTER, 
		SETTER;
	}
}
