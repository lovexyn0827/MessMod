package lovexyn0827.mess.util.access;

import java.lang.reflect.Type;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lovexyn0827.mess.util.TranslatableException;
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
		if(n.outputType instanceof Class<?>) {
			Class<?> out = (Class<?>) n.outputType;
			return VECTOR_CLASSES.stream().anyMatch(out::isAssignableFrom);
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
		
		if (obj == null || MethodNode.class != obj.getClass()) {
			return false;
		}
		
		return true;
	}

	@Override
	protected Class<?> prepare(Type lastOutType) {
		this.outputType = double.class;
		return double.class;
	}

	static class X extends ComponentNode {

		@Override
		Object access(Object previous) {
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
				throw new TranslatableException("exp.invalidlast", this);
			}
		}
		
		@Override
		public String toString() {
			return "x";
		}
	}
	
	static class Y extends ComponentNode {
		@Override
		boolean canFollow(Node n) {
			return super.canFollow(n) && n.outputType instanceof Class<?> 
					&& ChunkPos.class.isAssignableFrom((Class<?>) n.outputType);
		}

		@Override
		Object access(Object previous) {
			if(previous instanceof Entity) {
				return ((Entity) previous).getY();
			} else if(previous instanceof Vec3d) {
				return ((Vec3d) previous).y;
			} else if(previous instanceof Vec3i) {
				return ((Vec3i) previous).getY();
			} else if(previous instanceof BlockEntity) {
				return ((BlockEntity) previous).getPos().getY();
			} else {
				throw new TranslatableException("exp.invalidlast", this);
			}
		}
		
		@Override
		public String toString() {
			return "y";
		}
	}
	
	static class Z extends ComponentNode {

		@Override
		Object access(Object previous) {
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
				throw new TranslatableException("exp.invalidlast", this);
			}
		}
		
		@Override
		public String toString() {
			return "z";
		}
	}
}
