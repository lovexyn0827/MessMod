package mc.lovexyn0827.mcwmem.hud.data;

public enum EntityHudInfoType {
	ID("ID:",DataType.INTEGER),
	NAME("Name:",DataType.STRING),
	POS_X("Pos X:",DataType.DOUBLE),
	POS_Y("Pos Y:",DataType.DOUBLE),
	POS_Z("Pos Z:",DataType.DOUBLE),
	AGE("Age:",DataType.INTEGER),
	MOTION_X("Motion X:",DataType.DOUBLE),
	MOTION_Y("Motion Y",DataType.DOUBLE),
	MOTION_Z("Motion Z:",DataType.DOUBLE),
	DELTA_X("Delta X",DataType.DOUBLE),
	DELTA_Y("Delta Y",DataType.DOUBLE),
	DELTA_Z("Delta Z",DataType.DOUBLE),
	YAW("Yaw",DataType.FLOAT),
	PITCH("Pitch",DataType.FLOAT),
	FALL_DISTANCE("Fall Distance:",DataType.FLOAT),
	GENERAL_FLAGS("State:",DataType.STRING),
	FORWARD("Forward:",DataType.FLOAT),
	SIDEWAYS("SideWays:",DataType.FLOAT),
	UPWARD("Upwards:",DataType.FLOAT),
	HEALTH("Health:",DataType.FLOAT),
	LIVING_FLAGS("Living State:",DataType.STRING),
	MOVEMENT_SPEED("OnLand Speed",DataType.FLOAT),
	FLYING_SPEED("Fly Speed",DataType.FLOAT),
	FUSE("Fuse:",DataType.INTEGER),
	POWER_X("Power X",DataType.DOUBLE),
	POWER_Y("Power Y:",DataType.DOUBLE),
	POWER_Z("Power Z:",DataType.DOUBLE),
	VELOCITY_DECAY("Decay:",DataType.FLOAT);
	
	public String header;
	public DataType type;
	
	EntityHudInfoType(String header,DataType type){
		this.header = header;
		this.type = type;
	}
}
