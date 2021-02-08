package mc.lovexyn0827.mcwmem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetPoiCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> sp = (ct,builder)->{
			for(PointOfInterestType poi:Registry.POINT_OF_INTEREST_TYPE) {
				builder = builder.suggest(poi.toString());
			}
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("setpoi").requires((source)->source.hasPermissionLevel(1)).
				then(argument("pos",BlockPosArgumentType.blockPos()).
						then(argument("type",StringArgumentType.word()).suggests(sp).
								then(argument("replace",BoolArgumentType.bool()).
										executes((ct)->{
											if(setPoi(ct.getSource().getWorld().getPointOfInterestStorage(),
												BlockPosArgumentType.getBlockPos(ct, "pos"),														StringArgumentType.getString(ct, "type"),
															BoolArgumentType.getBool(ct, "replace"))) {
														ct.getSource().sendFeedback(new LiteralText("Added POI"), false);
														return 1;
													} else {
														ct.getSource().sendError(new LiteralText("POI has alerady existed"));
														return 0;
													}
												}))));
		dispatcher.register(command);
	}

	private static boolean setPoi(PointOfInterestStorage poiStorage, BlockPos blockPos, String type, boolean replace) {
		if(poiStorage.getType(blockPos).isPresent()&&!replace) return false;
		poiStorage.remove(blockPos);
		poiStorage.add(blockPos, Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(type)));
		return true;
	}
}
