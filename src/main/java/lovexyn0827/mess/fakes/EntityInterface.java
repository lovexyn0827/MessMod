package lovexyn0827.mess.fakes;

public interface EntityInterface {
	boolean isFrozen();
	void setFrozen(boolean frozen);
	boolean isStepHeightDisabled();
	void setStepHeightDisabled(boolean disabled);
	boolean shouldLogMovement();
	void setMovementSubscribed(boolean subscribed);
}
