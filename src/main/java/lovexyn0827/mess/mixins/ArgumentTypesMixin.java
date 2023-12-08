package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.command.CommandUtil;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.Registry;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMixin {
	@Inject(
			method = "register(Lnet/minecraft/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;", 
			at = @At("RETURN")
	)
	private static void registerArgumentTypes(Registry<?> reg, CallbackInfoReturnable<ArgumentSerializer<?, ?>> cir) {
		CommandUtil.registerArgumentTypes();
	}
}
