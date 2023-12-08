package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.command.EnumSetArgumentType;
import lovexyn0827.mess.command.ExtendedFloatArgumentType;
import lovexyn0827.mess.command.FilteredSetArgumentType;
import lovexyn0827.mess.util.access.AccessingPathArgumentType;
import lovexyn0827.mess.util.phase.TickingPhaseArgumentType;
import net.minecraft.command.argument.ArgumentTypes;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMixin {
	@Inject(method = "register()V", at = @At(value = "HEAD"))
	private static void addMessModArgumentTypes(CallbackInfo ci) {
		EnumSetArgumentType.registerArgumentType();
		ExtendedFloatArgumentType.registerArgumentType();
		FilteredSetArgumentType.registerArgumentType();
		AccessingPathArgumentType.registerArgumentType();
		TickingPhaseArgumentType.registerArgumentType();
	}
}
