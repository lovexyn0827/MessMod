package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.common.collect.ImmutableSet;

import lovexyn0827.mess.util.Reflection;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

abstract class ComponentNode extends Node {
	private static final Set<Class<?>> VECTOR_CLASSES = ImmutableSet.of(Vec3d.class, 
			Entity.class, Vec3i.class, ChunkPos.class, BlockEntity.class);
	
	@Override
	boolean canFollow(Node n) {
		Class<?> rawType = Reflection.getRawType(n.outputType);
		if(rawType != null) {
			return VECTOR_CLASSES.stream().anyMatch((c) -> c.isAssignableFrom(rawType));
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getClass().hashCode() ^ (this.outputType != null ? this.outputType.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		
		return true;
	}

	@Override
	protected Class<?> resolveOutputType(Type lastOutType) {
		return double.class;
	}

	static class X extends ComponentNode {
		@Override
		Object access(Object previous) throws AccessingFailureException {
			if(previous instanceof Entity) {
				return ((Entity) previous).getX();
			} else if(previous instanceof Vec3d) {
				return ((Vec3d) previous).x;
			} else if(previous instanceof Vec3i) {
				return ((Vec3i) previous).getX();
			} else if(previous instanceof ChunkPos) {
				return ((ChunkPos) previous).x;
			} else if(previous instanceof BlockEntity) {
				return ((BlockEntity) previous).getPos().getX();
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
			}
		}
		
		@Override
		public String toString() {
			return "x";
		}

		@Override
		NodeCompiler getCompiler() {
			return (ctx) -> {
				Type type = ctx.getLastOutputClass();
				InsnList insns = new InsnList();
				Class<?> rawType = Reflection.getRawType(type);
				if(Entity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/entity/Entity", "getX", "()D"));
				} else if(Vec3d.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3d"));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, 
							"net/minecraft/util/math/Vec3d", "x", "D"));
				} else if(Vec3i.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3i"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getX", "()I"));
				} else if(ChunkPos.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/ChunkPos"));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, 
							"net/minecraft/util/math/ChunkPos", "x", "D"));
				} else if(BlockEntity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/block/entity/BlockEntity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/block/entity/BlockEntity", "getPos", 
							"()Lnet/minecraft/util/math/BlockPos"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getX", "()I"));
				} else {
					throw new CompilationException(FailureCause.INV_LAST, this);
				}
				
				return insns;
			};
		}
	}
	
	static class Y extends ComponentNode {
		@Override
		boolean canFollow(Node n) {
			return super.canFollow(n) && n.outputType instanceof Class<?> 
					&& !ChunkPos.class.isAssignableFrom((Class<?>) n.outputType);
		}

		@Override
		Object access(Object previous) throws AccessingFailureException {
			if(previous instanceof Entity) {
				return ((Entity) previous).getY();
			} else if(previous instanceof Vec3d) {
				return ((Vec3d) previous).y;
			} else if(previous instanceof Vec3i) {
				return ((Vec3i) previous).getY();
			} else if(previous instanceof BlockEntity) {
				return ((BlockEntity) previous).getPos().getY();
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
			}
		}
		
		@Override
		public String toString() {
			return "y";
		}

		@Override
		NodeCompiler getCompiler() {
			return (ctx) -> {
				Type type = ctx.getLastOutputType();
				InsnList insns = new InsnList();
				Class<?> rawType = Reflection.getRawType(type);
				if(Entity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/entity/Entity", "getY", "()D"));
				} else if(Vec3d.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3d"));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, 
							"net/minecraft/util/math/Vec3d", "y", "D"));
				} else if(Vec3i.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3i"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getY", "()I"));
				} else if(BlockEntity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/block/entity/BlockEntity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/block/entity/BlockEntity", "getPos", 
							"()Lnet/minecraft/util/math/BlockPos"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getY", "()I"));
				} else {
					throw new CompilationException(FailureCause.INV_LAST, this);
				}
				
				return insns;
			};
		}
	}
	
	static class Z extends ComponentNode {

		@Override
		Object access(Object previous) throws AccessingFailureException {
			if(previous instanceof Entity) {
				return ((Entity) previous).getZ();
			} else if(previous instanceof Vec3d) {
				return ((Vec3d) previous).z;
			} else if(previous instanceof Vec3i) {
				return ((Vec3i) previous).getZ();
			} else if(previous instanceof ChunkPos) {
				return ((ChunkPos) previous).z;
			} else if(previous instanceof BlockEntity) {
				return ((BlockEntity) previous).getPos().getZ();
			} else {
				throw AccessingFailureException.createWithArgs(FailureCause.INV_LAST, this, null, this);
			}
		}
		
		@Override
		public String toString() {
			return "z";
		}

		@Override
		NodeCompiler getCompiler() {
			return (ctx) -> {
				Type type = ctx.getLastOutputType();
				InsnList insns = new InsnList();
				Class<?> rawType = Reflection.getRawType(type);
				Class<?> out;
				if(Entity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/Entity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/entity/Entity", "getZ", "()D"));
					out = double.class;
				} else if(Vec3d.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3d"));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, 
							"net/minecraft/util/math/Vec3d", "z", "D"));
					out = double.class;
				} else if(Vec3i.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/Vec3i"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getZ", "()I"));
					out = int.class;
				} else if(ChunkPos.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/ChunkPos"));
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, 
							"net/minecraft/util/math/ChunkPos", "z", "D"));
					out = int.class;
				} else if(BlockEntity.class.isAssignableFrom(rawType)) {
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/block/entity/BlockEntity"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/block/entity/BlockEntity", "getPos", 
							"()Lnet/minecraft/util/math/BlockPos"));
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 
							"net/minecraft/util/math/Vec3i", "getZ", "()I"));
					out = int.class;
				} else {
					throw new CompilationException(FailureCause.INV_LAST, this);
				}
				
				ctx.endNode(out);
				return insns;
			};
		}
	}
}
