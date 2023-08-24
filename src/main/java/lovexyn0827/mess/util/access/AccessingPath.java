package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.options.EnumParser;

/**
 * A sequence of {@link Node}, used to get some fields or elements from an object.
 * @author lovexyn0827
 * Date: April 22, 2022
 */
public interface AccessingPath {
	public static final AccessingPath DUMMY = new JavaAccessingPath(Collections.emptyList(), "");
	
	Object access(Object start, Type inputType) throws AccessingFailureException;
	
	/**
	 * Write a new value to the last node of the path.
	 * @param value The string representation of the new value, in the form of literals.
	 * @throws CommandSyntaxException 
	 */
	void write(Object start, Type genericType, String valueStr)
			throws AccessingFailureException, CommandSyntaxException;
	Type getOutputType();
	Class<?> compile(List<Class<?>> nodeInputTypes, String name) throws CompilationException;
	String getOriginalStringRepresentation();
	
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
