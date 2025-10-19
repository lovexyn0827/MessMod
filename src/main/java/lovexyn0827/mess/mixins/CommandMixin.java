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

import lovexyn0827.mess.command.AccessingPathCommand;
import lovexyn0827.mess.command.CountEntitiesCommand;
import lovexyn0827.mess.command.DrawShapeCommand;
import lovexyn0827.mess.command.EnsureCommand;
import lovexyn0827.mess.command.EntityConfigCommand;
import lovexyn0827.mess.command.EntityFieldCommand;
import lovexyn0827.mess.command.EntityLogCommand;
import lovexyn0827.mess.command.ExplodeCommand;
import lovexyn0827.mess.command.ExportSaveCommand;
import lovexyn0827.mess.command.FillInventoryCommand;
import lovexyn0827.mess.command.FreezeEntityCommand;
import lovexyn0827.mess.command.EntitySidebarCommand;
import lovexyn0827.mess.command.HudCommand;
import lovexyn0827.mess.command.LagCommand;
import lovexyn0827.mess.command.LazyLoadCommand;
import lovexyn0827.mess.command.LoadJavaAgentCommand;
import lovexyn0827.mess.command.LogChunkBehaviorCommand;
import lovexyn0827.mess.command.LogDeathCommand;
import lovexyn0827.mess.command.LogMovementCommand;
import lovexyn0827.mess.command.LogPacketCommand;
import lovexyn0827.mess.command.MessCfgCommand;
import lovexyn0827.mess.command.ModifyCommand;
import lovexyn0827.mess.command.MoveEntityCommand;
import lovexyn0827.mess.command.NameEntityCommand;
import lovexyn0827.mess.command.NameItemCommand;
import lovexyn0827.mess.command.PartlyKillCommand;
import lovexyn0827.mess.command.PoiCommand;
import lovexyn0827.mess.command.RaycastCommand;
import lovexyn0827.mess.command.RepeatCommand;
import lovexyn0827.mess.command.RideCommand;
import lovexyn0827.mess.command.RngCommand;
import lovexyn0827.mess.command.SetBlockRawCommand;
import lovexyn0827.mess.command.SetExplosionBlockCommand;
import lovexyn0827.mess.command.StackEntityCommand;
import lovexyn0827.mess.command.TileEntityCommand;
import lovexyn0827.mess.command.TouchCommand;
import lovexyn0827.mess.command.VariableCommand;
import lovexyn0827.mess.command.WaveGenCommand;
import lovexyn0827.mess.options.OptionManager;
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
        HudCommand.register(this.dispatcher);
        NameEntityCommand.register(this.dispatcher);
        EntitySidebarCommand.register(this.dispatcher);
        RideCommand.register(this.dispatcher);
        LagCommand.register(this.dispatcher);
        FreezeEntityCommand.register(this.dispatcher);
        LogPacketCommand.register(this.dispatcher);
        PartlyKillCommand.register(this.dispatcher);
        AccessingPathCommand.register(this.dispatcher);
        ExportSaveCommand.register(this.dispatcher);
        LogChunkBehaviorCommand.register(this.dispatcher);
        LazyLoadCommand.register(this.dispatcher);
        CountEntitiesCommand.register(this.dispatcher);
        VariableCommand.register(this.dispatcher);
        LoadJavaAgentCommand.register(this.dispatcher);
        LogDeathCommand.register(this.dispatcher);
        StackEntityCommand.register(this.dispatcher);
        FillInventoryCommand.register(this.dispatcher);
        WaveGenCommand.register(this.dispatcher);
        TouchCommand.register(this.dispatcher);
        SetBlockRawCommand.register(this.dispatcher);
        NameItemCommand.register(this.dispatcher);
        DrawShapeCommand.register(this.dispatcher);
    }
    
    @Redirect(method = "execute", 
    		at = @At(
    				value = "INVOKE",
    				target = "org/apache/logging/log4j/Logger.isDebugEnabled()V", 
    				remap = false
    		),
    		require = 0
    )
    private boolean alwaysOutputStackTrace(Logger l) {
    	return true;
    }
    
    @Redirect(method = "<init>", 
    		at = @At(
    				value = "FIELD", 
    				target = "net/minecraft/server/command/CommandManager$RegistrationEnvironment.dedicated:Z"
    		)
    )
    private boolean modifyDedicated(CommandManager.RegistrationEnvironment env) {
    	return ((CommandManagerRegistrationEnvironmentAccessor)(Object) env).isDedicated() 
    			|| OptionManager.dedicatedServerCommands;
    }
}
