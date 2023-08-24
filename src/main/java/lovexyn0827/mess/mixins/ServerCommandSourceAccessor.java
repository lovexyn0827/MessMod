package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(ServerCommandSource.class)
public interface ServerCommandSourceAccessor {
	@Accessor("output")
	CommandOutput getOutput();
}
