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
import net.minecraft.util.math.Vec3d;

public abstract class Literal<T> {
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
	 * @throws InvalidLiteralException 
	 * @implNote If the value of the literal is primitive types, argument type shouldn't be used.
	 */
	@Nullable
	public abstract T get(Type type) throws InvalidLiteralException;
	
	public static Literal<?> parse(String strRep) throws CommandSyntaxException {
		switch(strRep.charAt(0)) {
		case '\"' : 
			return new StringL(new StringReader(strRep.substring(1)).readStringUntil('"'));
		case 'E' : 
			if(strRep.charAt(1) == '+') {
				return new EnumL(strRep.substring(2));
			}
		case 'S' : 
			if(strRep.charAt(1) == '+') {
				return new StaticFieldL(strRep.substring(2));
			}
		case 'C' : 
			if(strRep.charAt(1) == '+') {
				return new ClassL(strRep.substring(2));
			}
		case '[' : 
			return new BlockPosL(strRep);
		case '(' : 
			return new Vec3dL(strRep);
		case '<' : 
		default : 
			if("null".equals(strRep)) {
				return new NullL();
			} else if (strRep.matches("(\\+|-)?[0-9]*(?:\\.[0-9]*)?(D|F|L|I)?")) {
				switch(strRep.charAt(strRep.length() - 1)) {
				case 'D' : 
					return new DoubleL(strRep);
				case 'F' : 
					return new FloatL(strRep);
				case 'L' :  
					return new LongL(strRep);
				case 'I' : 
					return new IntL(strRep);
				default : 
					if(strRep.contains(".")) {
						return new DoubleL(strRep);
					} else {
						return new IntL(strRep);
					}
				}
			} else {
				boolean bool;
				if("true".equals(strRep)) {
					bool = true;
				} else if ("false".equals(strRep)) {
					bool = false;
				} else {
					throw new TranslatableException("exp.invliteral", strRep);
				}
				
				return new Literal<Boolean>(strRep) {
					@Override
					public Boolean get(Type type) {
						return bool;
					}
				};
			}
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
		public String get(Type clazz) {
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
		public Object get(Type clazz) throws InvalidLiteralException {
			if(this.compiled && this.fieldVal != null) {
				return this.fieldVal;
			}
			
			if(clazz == null) {
				throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null,  
						this.stringRepresentation);
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
						throw InvalidLiteralException.createWithArgs(FailureCause.ERROR, this, e, e);
					}
				} else {
					String[] clAndF = this.stringRepresentation.split("#");
					if(clAndF.length == 2) {
						String cN = MessMod.INSTANCE.getMapping().srgClass(clAndF[0].replace('/', '.'));
						try {
							Class<?> decC = Class.forName(MessMod.INSTANCE.getMapping().srgClass(cN));
							Field f2 = Reflection.getFieldFromNamed(decC, clAndF[1]);
							if(f2 != null) {
								f2.setAccessible(true);
								this.fieldVal = f2.get(null);
								this.compiled = Modifier.isFinal(f2.getModifiers());
								return this.fieldVal;
							}
						} catch (ClassNotFoundException e) {
							throw InvalidLiteralException.createWithArgs(FailureCause.NO_CLASS, this, e, 
									clAndF[0]);
							
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
							throw InvalidLiteralException.createWithArgs(FailureCause.ERROR, this, e, e);
						}
					} else {
						throw InvalidLiteralException.create(FailureCause.INV_STATIC, this);
					}
					
					throw InvalidLiteralException.createWithArgs(FailureCause.NO_FIELD, this, null, 
							clAndF[1], clAndF[0]);
				}
			} else {
				throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null, 
						this.stringRepresentation);
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
		public Enum<?> get(Type clazz) throws InvalidLiteralException {
			if(this.compiled && this.enumConstant != null) {
				return this.enumConstant;
			}
			
			if(clazz == null) {
				throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null, 
						this.stringRepresentation);
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

				throw InvalidLiteralException.createWithArgs(FailureCause.NO_FIELD, this, null, 
						this.stringRepresentation, clazz.getTypeName());
			} else {
				throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null, this.stringRepresentation);
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
		public Integer get(Type clazz) {
			return this.integer;
		}
	}
	
	static class BlockPosL extends Literal<BlockPos> {
		private BlockPos pos;
		
		protected BlockPosL(String strRep) {
			super(strRep);
			try {
				String[] comp = strRep.substring(1, strRep.length() - 1).split(",");
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
		public BlockPos get(Type clazz) {
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
		public Void get(Type clazz) {
			return null;
		}
		
	}
	
	static class DoubleL extends Literal<Double> {
		private Double number;
		
		protected DoubleL(String strRep) {
			super(strRep);
			try {
				this.number = Double.parseDouble(strRep);
				this.compiled = true;
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqnum", strRep);
			}
		}

		@Override
		public Double get(Type clazz) {
			return this.number;
		}
	}
	
	static class FloatL extends Literal<Float> {
		private Float number;
		
		protected FloatL(String strRep) {
			super(strRep);
			try {
				this.number = Float.parseFloat(strRep);
				this.compiled = true;
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqnum", strRep);
			}
		}

		@Override
		public Float get(Type clazz) {
			return this.number;
		}
	}
	
	static class LongL extends Literal<Long> {
		private Long number;
		
		protected LongL(String strRep) {
			super(strRep);
			try {
				this.number = Long.parseLong(strRep);
				this.compiled = true;
			} catch (NumberFormatException e) {
				throw new TranslatableException("exp.reqint", strRep);
			}
		}

		@Override
		public Long get(Type clazz) {
			return this.number;
		}
	}
	

	public static class ClassL extends Literal<Class<?>> {
		protected ClassL(String strRep) {
			super(strRep);
		}

		@Override
		public Class<?> get(Type type) throws InvalidLiteralException {
			String className = this.stringRepresentation.replace('/', '.');
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw InvalidLiteralException.createWithArgs(FailureCause.NO_CLASS, this, null, className);
			}
		}

	}
	
	public static class Vec3dL extends Literal<Vec3d> {
		private final Vec3d vec3d;
		
		protected Vec3dL(String strRep) {
			super(strRep);
			try {
				String[] comp = strRep.substring(1, strRep.length() - 1).split(",");
				double x = Double.parseDouble(comp[0]);
				double y = Double.parseDouble(comp[1]);
				double z = Double.parseDouble(comp[2]);
				this.vec3d = new Vec3d(x, y, z);
				this.compiled = true;
			} catch (Exception e) {
				throw new TranslatableException("exp.invalidvec3", strRep);
			}
		}

		@Override
		public Vec3d get(Type type) {
			return this.vec3d;
		}

	}
}
