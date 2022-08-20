package lovexyn0827.mess.fakes;

import net.minecraft.predicate.NumberRange.IntRange;

public interface EntitySelectorReaderInterface {
	void setIdRange(IntRange intRange);
	IntRange getIdRange();
}
