package lovexyn0827.mess.fakes;

import java.util.regex.Pattern;

import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange.IntRange;

public interface EntitySelectorReaderInterface {
	void setIdRange(IntRange intRange);
	IntRange getIdRange();
	void setSide(NetworkSide side);
	NetworkSide getSide();
	void setTypeRegex(Pattern typeRegex);
	Pattern getTypeRegex();
	void setNameRegex(Pattern nameRegex);
	Pattern getNameRegex();
	void setClassRegex(Pattern classRegex);
	Pattern getClassRegex();
}
