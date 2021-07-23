package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.rendering.hud.HudManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;

public class MCWMEMCommand {
	private static final String NO_CARPET_ERROR = "Please install the carpet mod!";
	public static int rayLife = 300;
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggest = (ct,builder)->{
			return builder.suggest("topLeft").suggest("topRight").suggest("bottomLeft").suggest("bottomRight").buildFuture();
			
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("mcwmem").
				executes((ct)->{
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("mcwmem").get().getMetadata();
					CommandUtil.feedback(ct, metadata.getName());
					CommandUtil.feedback(ct, "Version:"+metadata.getVersion());
					CommandUtil.feedback(ct, metadata.getDescription());
					return 1;
				}).
				then(literal("setHudDisplay").
						then(argument("location",StringArgumentType.string()).suggests(suggest ).
								executes((ct)->{
									HudManager.AlignMode newAlignMode = MCWMEMod.INSTANCE.hudManager.hudAlign;
									String location = StringArgumentType.getString(ct, "location");
									newAlignMode = HudManager.AlignMode.fromString(location);
									if(newAlignMode==null) {
										CommandUtil.error(ct, "Undefined location:"+location);
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
						})).
				then(literal("entityExplosionRaysVisiblity").
						then(argument("boolean",BoolArgumentType.bool()).
								executes((ct) -> {
									MCWMEMod.INSTANCE.setOption("entityExplosionRaysVisiblity", 
											String.valueOf(BoolArgumentType.getBool(ct, "boolean")));
									return 1;
									
								})).
						then(literal("setLifetime").
								then(argument("ticks",IntegerArgumentType.integer()).
										executes((ct)->{
											int lifeTime = IntegerArgumentType.getInteger(ct, "ticks");
											rayLife = lifeTime;
											MCWMEMod.INSTANCE.setOption("entityExplosionRayLife", String.valueOf(lifeTime));
											return 1;
										})))).
				then(literal("serverSyncedBox").
						then(argument("boolean",BoolArgumentType.bool()).
								executes((ct) -> {
									boolean bool = BoolArgumentType.getBool(ct, "boolean");
									MCWMEMod.INSTANCE.setOption("serverSyncedBox", 
										String.valueOf(bool));
									return 1;
								}))).
				then(literal("mobFastKill").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct)->{
									MCWMEMod.INSTANCE.setOption("mobFastKill", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("enabledTools").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct)->{
									if(!FabricLoader.getInstance().isModLoaded("carpet")) {
										CommandUtil.error(ct, NO_CARPET_ERROR);
										return -1;
									}
									boolean bool = BoolArgumentType.getBool(ct, "bool");
									MCWMEMod.INSTANCE.setOption("enabledTools", String.valueOf(bool));
									if(bool) {
										CommandUtil.execute(ct.getSource(),"/script load tool");
									}else {
										CommandUtil.execute(ct.getSource(),"/script unload tool");
									}
									return 1;
								}))).
				then(literal("entityExplosionInfluence").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct)->{
									//CommandUtil.error(ct, "Not available now.");
									//if(true) return -1;
									MCWMEMod.INSTANCE.setOption("entityExplosionInfluence", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								})));
		dispatcher.register(command);
	}
}
