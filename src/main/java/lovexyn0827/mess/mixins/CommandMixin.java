package lovexyn0827.mess.mixins;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import lovexyn0827.mess.command.EnsureCommand;
import lovexyn0827.mess.command.EntityConfigCommand;
import lovexyn0827.mess.command.EntityFieldCommand;
import lovexyn0827.mess.command.EntityLogCommand;
import lovexyn0827.mess.command.ExplodeCommand;
import lovexyn0827.mess.command.LogMovementCommand;
import lovexyn0827.mess.command.MessCfgCommand;
import lovexyn0827.mess.command.ModifyCommand;
import lovexyn0827.mess.command.MoveEntityCommand;
import lovexyn0827.mess.command.PoiCommand;
import lovexyn0827.mess.command.RaycastCommand;
import lovexyn0827.mess.command.RepeatCommand;
import lovexyn0827.mess.command.RngCommand;
import lovexyn0827.mess.command.SetExplosionBlockCommand;
import lovexyn0827.mess.command.TileEntityCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandManager.class)
public abstract class CommandMixin {
	@Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerCommand(CommandManager.RegistrationEnvironment regEnv, CallbackInfo info) {
        ExplodeCommand.register(this.dispatcher);
        ModifyCommand.register(this.dispatcher);
        PoiCommand.register(this.dispatcher);
        SetExplosionBlockCommand.register(this.dispatcher);
        EntityFieldCommand.register(this.dispatcher);
        MessCfgCommand.register(this.dispatcher);
        RngCommand.register(this.dispatcher);
        TileEntityCommand.register(this.dispatcher);
        EntityConfigCommand.register(this.dispatcher);
        MoveEntityCommand.register(this.dispatcher);
        RaycastCommand.register(this.dispatcher);
        RepeatCommand.register(this.dispatcher);
        EntityLogCommand.register(this.dispatcher);
        EnsureCommand.register(this.dispatcher);
        LogMovementCommand.register(this.dispatcher);
    }
    
    @Redirect(method = "execute", at = @At(
    		value = "INVOKE",
    		target = "org/apache/logging/log4j/Logger.isDebugEnabled()V"),
    		require = 0)
    private boolean alwaysOutputStackTrace(Logger l) {
    	return true;
    }
}
