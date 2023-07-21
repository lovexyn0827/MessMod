package lovexyn0827.mess.util.access;

public enum FailureCause {
	/**
	 * Field %s couldn't be found in %s
	 * Arguments: name of the field, name of the class
	 */
	NO_FIELD("exp.nofield"), 
	
	/**
	 * Method %s couldn't be found in %s
	 */
	NO_METHOD("exp.nomethod"), 
	
	/**
	 * The value associated with the given key is not found!
	 */
	NO_KEY("exp.nokey"), 
	
	/**
	 * Out of bound!
	 */
	OUT_OF_BOUND("exp.outbound"), 
	
	/**
	 * A NullPointerException was thrown, possibly because the output of the last node is null.
	 */
	NULL("exp.null"), 
	
	/**
	 * Exception in executing method %s: %s
	 */
	INVOKE_FAIL("exp.failexec"), 
	
	/**
	 * Properity %s couldn't be gotten from the last node
	 */
	INV_LAST("exp.invalidlast"), 
	
	/**
	 * The declaring class of %s couldn't be known
	 */
	UNCERTAIN_CLASS("exp.unboundedclass"), 
	
	/**
	 * "Class %s is not found
	 */
	NO_CLASS("exp.noclass"), 
	
	/**
	 * "The result of the previous node of %s is not a Map!
	 */
	NOT_MAP("exp.notmap"), 
	
	/**
	 * Argument %s is illegal for %s
	 */
	BAD_ARG("exp.badarg"), 
	
	/**
	 * Multiple targets were found
	 */
	MULTI_TARGET("exp.multitarget"), 
	
	/**
	 * Use # to separate class name and field name, and use / to separate package names, like \"java/lang/FLOAT#MAX_VALUE\"
	 */
	INV_STATIC("exp.staticl.format"), 
	
	/**
	 * %s couldn't be casted to %s.
	 */
	CAST("exp.cast"), 
	
	/**
	 * Unexpected exception: %s
	 */
	ERROR("exp.unexc"), 
	
	/**
	 * Writting is not available.
	 */
	NOT_WRITTABLE("exp.nowrite"), 
	
	/**
	 * %s couldn't be set to %s
	 */
	INV_LAST_W("exp.invalidlastw");
	
	public final String translationKey;
	
	FailureCause(String translationKey) {
		this.translationKey = translationKey;
	}
}