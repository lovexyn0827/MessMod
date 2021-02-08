package mc.lovexyn0827.mcwmem.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import mc.lovexyn0827.mcwmem.command.BiomeCommand;
import mc.lovexyn0827.mcwmem.command.EntityFieldCommand;
import mc.lovexyn0827.mcwmem.command.ExplodeCommand;
import mc.lovexyn0827.mcwmem.command.MCWMEMCommand;
import mc.lovexyn0827.mcwmem.command.ModifyCommand;
import mc.lovexyn0827.mcwmem.command.SetExplosionBlockCommand;
import mc.lovexyn0827.mcwmem.command.SetPoiCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandManager.class)
public class CommandMixin {
	@Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerCommand(CommandManager.RegistrationEnvironment regEnv, CallbackInfo info) {
        ExplodeCommand.register(this.dispatcher);
        ModifyCommand.register(this.dispatcher);
        SetPoiCommand.register(this.dispatcher);
        SetExplosionBlockCommand.register(this.dispatcher);
        EntityFieldCommand.register(this.dispatcher);
        MCWMEMCommand.register(this.dispatcher);
        BiomeCommand.register(this.dispatcher);
    }
}
