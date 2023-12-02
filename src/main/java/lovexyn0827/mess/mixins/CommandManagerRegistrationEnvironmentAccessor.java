package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.command.CommandManager;

@Mixin(CommandManager.RegistrationEnvironment.class)
public interface CommandManagerRegistrationEnvironmentAccessor {
	@Accessor("dedicated")
	boolean isDedicated();
}
