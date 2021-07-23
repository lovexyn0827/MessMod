package mc.lovexyn0827.mcwmem.rendering.hud.data;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.EntityPose;

public enum DataType {
	INTEGER{
		@Override
		public String getStringOf(Object ob) {
			return ((Integer)ob).toString();
		}
	},
	FLOAT{
		@Override
		public String getStringOf(Object ob) {
			return ((Float)ob).toString();
		}
	},
	DOUBLE{
		@Override
		public String getStringOf(Object ob) {
			return ((Double)ob).toString();
		}
	},
	STRING{
		@Override
		public String getStringOf(Object ob) {
			return (String) ob;
		}
	},
	POSE{
		@Override
		public String getStringOf(Object ob) {
			return POSE_NAMES.get(ob);
		}
	};
	
	public abstract String getStringOf(Object ob);
	private static ImmutableMap<EntityPose, String> POSE_NAMES = new ImmutableMap.Builder<EntityPose, String>()
			.put(EntityPose.CROUCHING, "crouching")
			.put(EntityPose.DYING, "dying")
			.put(EntityPose.FALL_FLYING, "fall_flying")
			.put(EntityPose.SLEEPING, "sleeping")
			.put(EntityPose.SPIN_ATTACK, "spin_attack")
			.put(EntityPose.STANDING, "standing")
			.put(EntityPose.SWIMMING, "swiming")
			.build();
}
