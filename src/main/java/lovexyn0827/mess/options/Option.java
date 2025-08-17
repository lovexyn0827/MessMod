package lovexyn0827.mess.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fabricmc.api.EnvType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Option {
	String defaultValue();
	String[] suggestions() default {};
	
	/**
	 * @return true if this option is currently unfinished or buggy.
	 */
	boolean experimental() default false;
	Class<? extends OptionParser<?>> parserClass();
	boolean globalOnly() default false;
	EnvType[] environment() default {EnvType.CLIENT, EnvType.SERVER};
	Label[] label();
	boolean deprecated() default false;
}
