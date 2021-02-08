package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.hud.HudManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class MCWMEMCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggest = (ct,builder)->{
			return builder.suggest("topLeft").suggest("topRight").suggest("bottomLeft").suggest("bottomRight").buildFuture();
			
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("mcwmem").
				then(literal("setHudDisplay").
						then(argument("location",StringArgumentType.string()).suggests(suggest ).
								executes((ct)->{
									HudManager.AlignMode newAlignMode = MCWMEMod.INSTANCE.hudManager.hudAlign;
									String location = StringArgumentType.getString(ct, "location");
									newAlignMode = HudManager.AlignMode.fromString(location);
									if(newAlignMode==null) {
										ct.getSource().sendError(new LiteralText("Undieined location:"+location));
										return -1;
									}
									MCWMEMod.INSTANCE.hudManager.hudAlign = newAlignMode;
									MCWMEMod.INSTANCE.setOption("alignMode",location);
									return 1;
								}))).
				then(literal("reloadConfig").
						executes((ct)->{
							MCWMEMod.INSTANCE.getOption("foo");
							return 1;
						}));
		dispatcher.register(command);
	}
}
