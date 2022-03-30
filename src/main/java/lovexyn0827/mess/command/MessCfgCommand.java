package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Arrays;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.Options;
import lovexyn0827.mess.rendering.BlockInfoRenderer;
import lovexyn0827.mess.rendering.hud.HudManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.ServerCommandSource;

public class MessCfgCommand {
	private static final String NO_CARPET_ERROR = "Please install the carpet mod!";
	public static int rayLife = 300;
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// TODO Stop hardcoding the options.
		SuggestionProvider<ServerCommandSource> suggestHud = (ct, builder) -> {
			return builder.suggest("topLeft").suggest("topRight").suggest("bottomLeft").suggest("bottomRight").buildFuture();
			
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("messcfg").
				executes((ct) -> {
					ModMetadata metadata = FabricLoader.getInstance().getModContainer("just_a_mess").get().getMetadata();
					CommandUtil.feedback(ct, metadata.getName());
					CommandUtil.feedback(ct, "Version:" + metadata.getVersion());
					CommandUtil.feedback(ct, metadata.getDescription());
					CommandUtil.feedback(ct, "Current Options");
					Options option = MessMod.INSTANCE.options;
					option.forEach((k, v) -> CommandUtil.feedback(ct, k + " : " + v + (v.equals(option.getDefault((String) k)) ? "" : "   (Modified)")));
					return 1;
				}).
				then(literal("setHudDisplay").
						then(argument("location",StringArgumentType.string()).suggests(suggestHud).
								executes((ct) -> {
									HudManager.AlignMode newAlignMode = MessMod.INSTANCE.hudManager.hudAlign;
									String location = StringArgumentType.getString(ct, "location");
									newAlignMode = HudManager.AlignMode.fromString(location);
									if(newAlignMode == null) {
										CommandUtil.error(ct, "Undefined location:" + location);
										return -1;
									}
									MessMod.INSTANCE.hudManager.hudAlign = newAlignMode;
									MessMod.INSTANCE.setOption("alignMode", location);
									return 1;
								}))).
				then(literal("reloadConfig").
						executes((ct) -> {
							CommandUtil.feedback(ct, "Reloaded all configs");
							MessMod.INSTANCE.options.load();
							return 1;
						})).
				then(literal("entityExplosionRaysVisiblity").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("entityExplosionRaysVisiblity", 
											String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
									
								})).
						then(literal("setLifetime").
								then(argument("ticks",IntegerArgumentType.integer()).suggests(CommandUtil.immutableSuggestions("300")).
										executes((ct) -> {
											int lifeTime = IntegerArgumentType.getInteger(ct, "ticks");
											rayLife = lifeTime;
											MessMod.INSTANCE.setOption("entityExplosionRayLife", String.valueOf(lifeTime));
											return 1;
										})))).
				then(literal("serverSyncedBox").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									boolean bool = BoolArgumentType.getBool(ct, "bool");
									MessMod.INSTANCE.setOption("serverSyncedBox", 
										String.valueOf(bool));
									return 1;
								}))).
				then(literal("mobFastKill").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("mobFastKill", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("enabledTools").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									if(!FabricLoader.getInstance().isModLoaded("carpet")) {
										CommandUtil.error(ct, NO_CARPET_ERROR);
										return -1;
									}
									boolean bool = BoolArgumentType.getBool(ct, "bool");
									MessMod.INSTANCE.setOption("enabledTools", String.valueOf(bool));
									if(bool) {
										CommandUtil.execute(ct.getSource(),"/script load tool");
									}else {
										CommandUtil.execute(ct.getSource(),"/script unload tool");
									}
									return 1;
								}))).
				then(literal("entityExplosionInfluence").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									CommandUtil.error(ct, "Maybe not available now.");
									MessMod.INSTANCE.setOption("entityExplosionInfluence", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("renderBlockShape").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("renderBlockShape", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("renderFluidShape").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("renderFluidShape", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("blockShapeToBeRendered").
						then(argument("shapeType", StringArgumentType.word()).
								suggests((s, b) -> {
									Arrays.stream(BlockInfoRenderer.ShapeType.values()).
											map(Object::toString).
											forEach(b::suggest);
									return b.buildFuture();
								}).
								executes((ct) -> {
									String type = StringArgumentType.getString(ct, "shapeType");
									if(Arrays.stream(BlockInfoRenderer.ShapeType.values()).map((t) -> t.toString()).noneMatch(type::equals)) {
										CommandUtil.error(ct, "Invaild ShapeType : " + type);
										return 0;
									}
									
									MessMod.INSTANCE.setOption("blockShapeToBeRendered", type);
									return 1;
								}))).
				then(literal("tntChunkLoading").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("tntChunkLoading", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("projectileChunkLoading").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("projectileChunkLoading", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("maxClientTicksPerFrame").
						then(argument("ticks",IntegerArgumentType.integer(0)).suggests(CommandUtil.immutableSuggestions("10")).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("maxClientTicksPerFrame", String.valueOf(IntegerArgumentType.getInteger(ct, "ticks")));
									return 1;
								}))).
				then(literal("debugStickSkipsInvaildState").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("debugStickSkipsInvaildState", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									CommandUtil.feedback(ct, "This option doesn't work for some unknow reason, so try flipinCactus.");
									return 1;
								}))).
				then(literal("disableProjectileRandomness").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("disableProjectileRandomness", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("endEyeTeleport").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("endEyeTeleport", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								}))).
				then(literal("maxEndEyeTpRadius").
						then(argument("radius", FloatArgumentType.floatArg(0, 2048)).suggests(CommandUtil.immutableSuggestions("180", "128", "320")).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("maxEndEyeTpRadius", String.valueOf(FloatArgumentType.getFloat(ct, "radius")));
									return 1;
								}))).
				then(literal("creativeUpwardsSpeed").
						then(argument("speed", FloatArgumentType.floatArg()).suggests(CommandUtil.immutableSuggestions("0.05", "0.10")).
								executes((ct) -> {
									MessMod.INSTANCE.setOption("creativeUpwardsSpeed", String.valueOf(FloatArgumentType.getFloat(ct, "speed")));
									return 1;
								}))).
				then(literal("hudtextSize").
						then(argument("size", FloatArgumentType.floatArg()).suggests(CommandUtil.immutableSuggestions("1.0", "1.5", "1.8", "2.0")).
								executes((ct) -> {
									String size = String.valueOf(FloatArgumentType.getFloat(ct, "size"));
									MessMod.INSTANCE.setOption("hudtextSize", size);
									MessMod.INSTANCE.hudManager.setTextSize(Float.parseFloat(size));
									return 1;
								})));
				/*.
				then(literal("railNoAutoConnection").
						then(argument("bool",BoolArgumentType.bool()).
								executes((ct)->{
									MessMod.INSTANCE.setOption("railNoAutoConnection", String.valueOf(BoolArgumentType.getBool(ct, "bool")));
									return 1;
								})))*/
		dispatcher.register(command);
	}
}
