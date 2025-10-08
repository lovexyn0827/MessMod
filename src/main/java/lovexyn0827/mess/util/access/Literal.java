package lovexyn0827.mess.util.access;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.VariableCommand;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.Reflection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class Literal<T> {
	private static final Pattern NUMBER_PATTERN = Pattern
			.compile("^((?:\\+|-)?[0-9]*(?:\\.[0-9]*)?|(?:\\+|-)?Infinity|NaN)(?:D|F|L|I)?$");
	@NotNull
	protected final String stringRepresentation;
	/**
	 * Whether or not the value could be determined from the string representation directly
	 * and won't change since created or initialized for the first time.
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
		return this.compiled && other.compiled && this.stringRepresentation.equals(other.stringRepresentation);
	}
	
	@Override
	public String toString() {
		return this.stringRepresentation;
	}

	/**
	 * @param type The type which the literal is expected to be parsed as, or null if unknown.
	 * @throws InvalidLiteralException 
	 * @implNote For literals of primitive types, {@code type} shouldn't be used. No side-effects permitted.
	 */
	@Nullable
	public abstract T get(@Nullable Type type) throws InvalidLiteralException;
	
	public static Literal<?> parse(String strRep) throws CommandSyntaxException {
		// I & N are reserved for special floating-point numbers.
		switch(strRep.charAt(0)) {
		case '"' : 
			return new StringL(strRep);
		case 'E' : 
			if(strRep.charAt(1) == '+') {
				return new EnumL(strRep);
			}
			
			break;
		case 'S' : 
			if(strRep.charAt(1) == '+') {
				return new StaticFieldL(strRep);
			}
			
			break;
		case 'C' : 
			if(strRep.charAt(1) == '+') {
				return new ClassL(strRep);
			}
			
			break;
		case 'A' : 
			if (strRep.charAt(1) == '+') {
				return new ArrayL(strRep);
			}
			
			break;
		case '[' : 
			return new BlockPosL(strRep);
		case '(' : 
			return new Vec3dL(strRep);
		case 'V' : 
			if(strRep.charAt(1) == '+') {
				return new VarL(strRep);
			}
			
			break;
		default : 
			Matcher matcher = NUMBER_PATTERN.matcher(strRep);
			if("null".equals(strRep)) {
				return new NullL();
			} else if (matcher.matches()) {
				String numStr = matcher.group(1);
				switch(strRep.charAt(strRep.length() - 1)) {
				case 'D' : 
					return new DoubleL(numStr);
				case 'F' : 
					return new FloatL(numStr);
				case 'L' :  
					return new LongL(numStr);
				case 'I' : 
					return new IntL(numStr);
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
		
		throw new TranslatableException("exp.invliteral", strRep);
	}

	/**
	 * @return A Literal instance which can be reinitialized for other inputs
	 */
	protected Literal<?> recreate() {
		return this;
	}
	
	/**
	 * @return Whether or not the literal is immutable and always represent the same value in a single run.
	 */
	public boolean isStatic() {
		return this.compiled;
	}
	
	public String serialize() {
		return this.stringRepresentation;
	}
	
	static class StringL extends Literal<String> {
		private String string;
		
		protected StringL(String strRep) {
			super(strRep);
			this.string = strRep.substring(1, strRep.length() - 1);
			this.compiled = true;
		}

		@Override
		public String get(Type clazz) {
			return this.string;
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
			
			Class<?> cl = Reflection.getRawType(clazz);
			String[] clAndF = this.stringRepresentation.substring(2).split("#");
			if(clAndF.length == 2) {
				String cN = MessMod.INSTANCE.getMapping().srgClass(clAndF[0].replace('/', '.'));
				try {
					Class<?> decC = Class.forName(MessMod.INSTANCE.getMapping().srgClass(cN));
					Field f = Reflection.getFieldFromNamed(decC, clAndF[1]);
					if(f != null) {
						f.setAccessible(true);
						this.fieldVal = f.get(null);
						this.compiled = Modifier.isFinal(f.getModifiers());
						return this.fieldVal;
					} else {
						throw InvalidLiteralException.createWithArgs(FailureCause.NO_FIELD, this, null, 
								clAndF[1], clAndF[0]);
					}
				} catch (ClassNotFoundException e) {
					throw InvalidLiteralException.createWithArgs(FailureCause.NO_CLASS, this, e, clAndF[0]);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					throw InvalidLiteralException.createWithArgs(FailureCause.ERROR, this, e, e);
				}
			} else if(clAndF.length == 1) {
				String fieldName = this.stringRepresentation.substring(2);
				if(cl != null) {
					// Try simple mode (E+FIELD_NAME)
					Field f = Reflection.getFieldFromNamed(cl, fieldName);
					if(f != null && Modifier.isStatic(f.getModifiers())) {
						f.setAccessible(true);
						try {
							this.fieldVal = f.get(null);
							this.compiled = Modifier.isFinal(f.getModifiers());
							return this.fieldVal;
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
							throw InvalidLiteralException.createWithArgs(FailureCause.ERROR, this, e, e);
						}
					} else {
						throw InvalidLiteralException.createWithArgs(FailureCause.NO_FIELD, this, null, 
								clazz == null ? "???" : clazz.getTypeName(), fieldName);
					}
				} else {
					throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null, 
							fieldName);
				}
			} else {
				throw InvalidLiteralException.create(FailureCause.INV_STATIC, this);
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
			if(this.compiled) {
				return this.enumConstant;
			}
			
			if(clazz == null) {
				throw InvalidLiteralException.createWithArgs(FailureCause.UNCERTAIN_CLASS, this, null, 
						this.stringRepresentation);
			}
			
			Class<?> cl = Reflection.getRawType(clazz);
			if(cl != null && cl.isEnum()) {
				String f = MessMod.INSTANCE.getMapping().srgField(cl.getName(), this.stringRepresentation);
				try {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Enum<?> e = Enum.valueOf((Class) cl, f);
					this.enumConstant = e;
					this.compiled = true;
					return e;
				} catch (IllegalArgumentException e) {
					throw InvalidLiteralException.createWithArgs(FailureCause.NO_FIELD, this, null, 
							this.stringRepresentation, clazz.getTypeName());
				}
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
		private Class<?> classVal;
		
		protected ClassL(String strRep) {
			super(strRep);
		}

		@Override
		public Class<?> get(Type type) throws InvalidLiteralException {
			if(this.compiled) {
				return this.classVal;
			}
			
			String className = this.stringRepresentation.substring(2).replace('/', '.');
			String srg = MessMod.INSTANCE.getMapping().srgClass(className);
			try {
				this.classVal = Reflection.getClassIncludingPrimitive(srg);
				this.compiled = true;
				return this.classVal;
			} catch (ClassNotFoundException e) {
				throw InvalidLiteralException.createWithArgs(FailureCause.NO_CLASS, this, e, className);
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
	
	public static class VarL extends Literal<Object> {
		private final String slot;

		protected VarL(String strRep) throws CommandSyntaxException {
			super(strRep);
			this.slot = strRep.substring(2);
			this.compiled = false;
		}

		@Override
		public @Nullable Object get(@Nullable Type type) throws InvalidLiteralException {
			return VariableCommand.getVariable(this.slot);
		}
		
	}
	
	// Returning Object since Object[] is not assignable from int[]
	public static class ArrayL extends Literal<Object> {
		private final Supplier<Object> arrayCreator;

		// ClazzSignature[dim0][dim1]...
		protected ArrayL(String strRep) throws CommandSyntaxException {
			super(strRep);
			this.compiled = false;
			// TODO Handle [int
			int dimListOffset = strRep.indexOf('[');
			if (dimListOffset == -1) {
				throw new TranslatableException("exp.atleast1dim");
			}

			// Parse class name
			String namedClass = strRep.substring(2, dimListOffset).replace('/', '.');
			String srg = MessMod.INSTANCE.getMapping().srgClass(namedClass);
			Class<?> clazz;
			try {
				clazz = Reflection.getClassIncludingPrimitive(srg);
			} catch (ClassNotFoundException e) {
				throw new TranslatableException("exp.noclass", namedClass);
			}
			
			// Parse dimensions
			IntList dims = new IntArrayList();
			StringReader sr = new StringReader(strRep.substring(dimListOffset));
			while (sr.canRead()) {
				sr.skipWhitespace();
				if (sr.read() != '[' || !sr.canRead()) {
					throw new TranslatableException("exp.invarray", strRep);
				}

				dims.add(sr.readInt());
				if (!sr.canRead() || sr.read() != ']') {
					throw new TranslatableException("exp.invarray", strRep);
				}
			}
			
			// Build array creator
			int[] dimensions = dims.toIntArray();
			
			this.arrayCreator = () -> {
				return Array.newInstance(clazz, dimensions);
			};
		}

		@Override
		public @Nullable Object get(@Nullable Type type) throws InvalidLiteralException {
			return this.arrayCreator.get();
		}
		
	}
}
