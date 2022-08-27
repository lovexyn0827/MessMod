package lovexyn0827.mess.fakes;

import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange.IntRange;

public interface EntitySelectorReaderInterface {
	void setIdRange(IntRange intRange);
	IntRange getIdRange();
	void setSide(NetworkSide side);
	NetworkSide getSide();
}
