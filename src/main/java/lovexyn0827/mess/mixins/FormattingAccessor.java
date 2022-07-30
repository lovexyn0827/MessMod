package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.Formatting;

@Mixin(Formatting.class)
public interface FormattingAccessor {
	@Accessor("code")
	char getCode();
}
