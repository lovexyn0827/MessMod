package lovexyn0827.mess.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option {
	String description();
	String defaultValue();
	String[] suggestions() default {};
	boolean experimental() default false;
	Class<? extends OptionParser<?>> parserClass();
}
