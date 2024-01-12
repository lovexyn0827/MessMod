package lovexyn0827.mess.rendering.hud.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

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
	
	// Very nasty solution, causing my Minecraft to crash...
	// However we cannot use the name() method as the names are obfuscated.
	private static final ImmutableMap<EntityPose, String> POSE_NAMES;
	
	static {
		POSE_NAMES = new ImmutableMap.Builder<EntityPose, String>()
				.put(EntityPose.STANDING, "standing")
				.put(EntityPose.FALL_FLYING, "fall_flying")
				.put(EntityPose.SLEEPING, "sleeping")
				.put(EntityPose.SWIMMING, "swiming")
				.put(EntityPose.CROUCHING, "crouching")
				.put(EntityPose.SPIN_ATTACK, "spin_attack")
				.put(EntityPose.LONG_JUMPING, "long_jumping")
				.put(EntityPose.CROAKING, "croaking")
				.put(EntityPose.USING_TONGUE, "using_tongue")
				.put(EntityPose.SITTING, "sitting")
				.put(EntityPose.ROARING, "roaring")
				.put(EntityPose.SNIFFING, "sniffing")
				.put(EntityPose.EMERGING, "emerging")
				.put(EntityPose.DIGGING, "digging")
				.put(EntityPose.DYING, "dying")
				.build();
		if(!POSE_NAMES.keySet().containsAll(Sets.newHashSet(EntityPose.values()))) {
			throw new IllegalStateException("Incomplete EntityPose map!");
		}
	}
}
