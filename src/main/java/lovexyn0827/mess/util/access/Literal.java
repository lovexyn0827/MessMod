package lovexyn0827.mess.util.access;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.Reflection;
import net.minecraft.util.math.BlockPos;

abstract class Literal<T> {
	@NotNull
	protected final String stringRepresentation;
	/**
	 * Whether or not the value could be determined from the string representation directly
	 * and won't change since created.
	 */
	protected boolean compiled;

	protected Literal(String strRep) {
		Objects.requireNonNull(strRep);
		this.stringRepresentation = strRep;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stringRepresentation == null) ? 0 : stringRepresentation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Literal<?> other = (Literal<?>) obj;
		return this.compiled && stringRepresentation.equals(other.stringRepresentation);
	}

	/**
	 * @throws AccessingFailureException 
	 * @implNote If the value of the literal is primitive types, argument type shouldn't be used.
	 */
	@Nullable
	abstract T get(Type type) throws AccessingFailureException;
	
	static Literal<?> parse(String strRep) throws CommandSyntaxException {
		if(strRep.startsWith("\"")) {
			return new StringL(new StringReader(strRep.substring(1)).readStringUntil('"'));
		} else if(strRep.startsWith("E+")) {
			return new EnumL(strRep.substring(2));
		} else if(strRep.startsWith("S+")) {
			return new StaticFieldL(strRep.substring(2));
		} else if(strRep.startsWith("[") && strRep.endsWith("]")) {
			return new BlockPosL(strRep);
		} else if("null".equals(strRep)) {
			return new NullL();
		} else {
			return new IntL(strRep);
		}
	}

	/**
	 * @return A Literal instance which can be reinitialized for other inputs
	 */
	protected Literal<?> recreate() {
		return this;
	}
	
	static class StringL extends Literal<String> {
		protected StringL(String strRep) {
			super(strRep);
		}

		@Override
		String get(Type clazz) {
			return this.stringRepresentation;
		}
	}
	
	static class StaticFieldL extends Literal<Object> {
		private Object fieldVal;
		
		protected StaticFieldL(String strRep) {
			super(strRep);
		}

		@Override
		@NotNull
		Object get(Type clazz) throws AccessingFailureException {
			if(this.compiled && this.fieldVal != null) {
				return this.fieldVal;
			}
			
			if(clazz == null) {
				throw new AccessingFailureException(AccessingFailureException.Cause.UNCERTAIN_CLASS, this.stringRepresentation);
			}
			
			Class<?> cl = Reflection.getRawType(clazz);
			if(cl != null) {
				Field f = Reflection.getFieldFromNamed(cl, this.stringRepresentation);
				if(f != null && Modifier.isStatic(f.getModifiers())) {
					f.setAccessible(true);
					try {
						return f.get(null);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
						throw new AccessingFailureException(AccessingFailureException.Cause.ERROR, e);
					}
				} else {
					String[] clAndF = this.stringRepresentation.split("#");
					if(clAndF.length == 2) {
						String cN = MessMod.INSTANCE.getMapping().srgClass(clAndF[0].replace('/', '.'));
						try {
							Class<?> decC = Class.forName(MessMod.INSTANCE.getMapping().srgClass(cN));
							Field f2 = Reflection.getFieldFromNamed(decC, clAndF[1]);
							if(f2 != null) {
								// XXX Use the stored value of modifiable field?
								f2.setAccessible(true);
								this.fieldVal = f2.get(null);
								this.compiled = Modifier.isFinal(f2.getModifiers());
								return this.fieldVal;
							}
						} catch (ClassNotFoundException e) {
							throw new AccessingFailureException(AccessingFailureException.Cause.NO_CLASS, clAndF[0]);
							
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
							throw new AccessingFailureException(AccessingFailureException.Cause.ERROR, e);
						}
					} else {
						throw new AccessingFailureException(AccessingFailureException.Cause.INV_STATIC);
					}
					
					throw new AccessingFailureException(AccessingFailureException.Cause.NO_FIELD, 
							clAndF[1], clAndF[0]);
				}
			} else {
				throw new AccessingFailureException(AccessingFailureException.Cause.UNCERTAIN_CLASS, this.stringRepresentation);
			}
		}

		@Override
		protected Literal<?> recreate() {
			return new StaticFieldL(this.stringRepresentation);
		}
	}
	
	static class EnumL extends Literal<Enum<?>> {
		private Enum<?> enumConstant;

		protected EnumL(String strRep) {
			super(strRep);
		}

		@Override
		Enum<?> get(Type clazz) throws AccessingFailureException {
			if(this.compiled && this.enumConstant != null) {
				return this.enumConstant;
			}
			
			if(clazz == null) {
				throw new AccessingFailureException(AccessingFailureException.Cause.UNCERTAIN_CLASS, this.stringRepresentation);
			}
			
			Class<?> cl = Reflection.getRawType(clazz);
			if(cl != null && cl.isEnum()) {
				String f = MessMod.INSTANCE.getMapping().srgField(cl.getName(), this.stringRepresentation);
				if(f != null) {
					Enum<?> e = Enum.valueOf(null, f);
					if(e != null) {
						this.enumConstant = e;
						this.compiled = true;
						return e;
					}
				}

				throw new AccessingFailureException(AccessingFailureException.Cause.NO_FIELD, 
						this.stringRepresentation, clazz.getTypeName());
			} else {
				throw new AccessingFailureException(AccessingFailureException.Cause.UNCERTAIN_CLASS, this.stringRepresentation);
			}
		}

		@Override
		protected Literal<?> recreate() {
			return new EnumL(this.stringRepresentation);
		}
	}
	
	static class IntL extends Literal<Integer> {
		private Integer integer;
		
		protected IntL(String strRep) {
			super(strRep);
			try {
				this.integer = Integer.parseInt(strRep);
				this.compiled = true;
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqint", strRep);
			}
		}

		@Override
		Integer get(Type clazz) {
			return this.integer;
		}
	}
	
	static class BlockPosL extends Literal<BlockPos> {
		private BlockPos pos;
		
		protected BlockPosL(String strRep) {
			super(strRep);
			try {
				String[] comp = strRep.substring(1, strRep.length()).split(",");
				int x = Integer.parseInt(comp[0]);
				int y = Integer.parseInt(comp[1]);
				int z = Integer.parseInt(comp[2]);
				this.pos = new BlockPos(x, y, z);
				this.compiled = true;
			} catch (Exception e) {
				throw new TranslatableException("exp.invalidbp", strRep);
			}
		}

		@Override
		BlockPos get(Type clazz) {
			return this.pos;
		}
	}
	
	static class NullL extends Literal<Void> {
		protected NullL() {
			super("null");
			this.compiled = true;
		}

		@Override
		@Nullable
		Void get(Type clazz) {
			return null;
		}
		
	}
}
