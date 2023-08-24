package lovexyn0827.mess.util.blame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.blame.AnalyzedCause.Clue;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.util.Pair;

public final class TraceElement {
	public final Class<?> clazz;
	public final String className;
	public final String methodName;
	public final int lineNum;
	public final Set<Executable> suspectedMethods;
	private final boolean isNotMc;
	
	private TraceElement(Class<?> clazz, String className, String methodName, 
			int lineNum, Set<Executable> suspectedMethods) {
		this.clazz = clazz;
		this.className = className;
		this.methodName = methodName;
		this.lineNum = lineNum;
		this.suspectedMethods = suspectedMethods;
		this.isNotMc = !this.clazz.getName().startsWith("net.minecraft");
	}
	
	public static TraceElement from(StackTraceElement e) {
		Class<?> cl;
		try {
			cl = Class.forName(e.getClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			throw new IllegalArgumentException(e1);
		}
		
		String mName = e.getMethodName();
		Set<Executable> susM = new HashSet<>();
		if("<init>".equals(mName)) {
			for(Constructor<?> c : cl.getDeclaredConstructors()) {
				susM.add(c);
			}
		} else if(!"<clinit>".equals(mName)) {
			for(Method m : cl.getDeclaredMethods()) {
				if(mName.equals(m.getName())) {
					susM.add(m);
				}
			}
		}
		
		return new TraceElement(cl, e.getClassName(), mName, e.getLineNumber(), susM);
	}
	
	public Set<AnalyzedCause.Clue> blame() {
		if(isNotMc) {
			return Collections.emptySet();
		}
		
		Set<Pair<Predicate<TraceElement>, Clue>> set = AnalyzedCause.Clue.BUILTIN.get(this.clazz);
		if(set == null) {
			return Collections.emptySet();
		} else {
			return set.stream()
					.filter((p) -> p.getLeft().test(this))
					.map(Pair::getRight)
					.collect(Collectors.toSet());
		}
	}
	
	public Confidence mentions(Executable m) {
		if(this.suspectedMethods.contains(m)) {
			return this.suspectedMethods.size() == 1 ? Confidence.DEFINITE : Confidence.POSSIBLE;
		} else {
			return Confidence.IMPOSSIBLE;
		}
	}
	
	public Confidence mentionsConsideringInherance(Executable m) {
		if(this.suspectedMethods.stream().anyMatch((sm) -> Reflection.isOverriding(sm, m))) {
			return this.suspectedMethods.size() == 1 ? Confidence.DEFINITE : Confidence.POSSIBLE;
		} else {
			return Confidence.IMPOSSIBLE;
		}
	}
	
	public Confidence withinLineRange(int minLine, int maxLine) {
		if(minLine < 0 || maxLine < 0 || minLine > maxLine) {
			throw new IllegalArgumentException(String.format("Invalid range: [%d, %d]", minLine, maxLine));
		}
		
		if(this.lineNum < 0) {
			return Confidence.UNLIKELY;
		} else {
			return this.lineNum <= maxLine && this.lineNum >= minLine ? Confidence.DEFINITE : Confidence.IMPOSSIBLE;
		}
	}
	
	public TraceElement mapToNamed() {
		Mapping map = MessMod.INSTANCE.getMapping();
		return new TraceElement(this.clazz, map.namedClass(this.className), 
				map.namedMethod(this.methodName, null), this.lineNum, this.suspectedMethods);
	}
}
