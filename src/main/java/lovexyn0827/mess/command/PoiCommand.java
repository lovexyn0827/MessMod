package lovexyn0827.mess.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;
import java.util.stream.Stream;

public class PoiCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> sp = (ct,builder)->{
			for(PointOfInterestType poi:Registry.POINT_OF_INTEREST_TYPE) {
				builder = builder.suggest(poi.toString());
			}
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("poi").requires((source)->source.hasPermissionLevel(1)).
				then(literal("set").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								then(argument("type",StringArgumentType.word()).suggests(sp).
										then(argument("replace",BoolArgumentType.bool()).
												executes((ct)->{
													if(setPoi(ct.getSource().getWorld().getPointOfInterestStorage(),
															BlockPosArgumentType.getBlockPos(ct, "pos"),														StringArgumentType.getString(ct, "type"),
															BoolArgumentType.getBool(ct, "replace"))) {
														CommandUtil.feedback(ct, "Modified POI");
														return 1;
													} else {
														CommandUtil.error(ct, "POI has alerady existed");
														return 0;
													}
												}))))).
				then(literal("get").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									PointOfInterestType type = getPoi(ct.getSource().getWorld().getPointOfInterestStorage(),
											BlockPosArgumentType.getBlockPos(ct, "pos"));
									CommandUtil.feedback(ct, type==null?"null":type.toString());
									return 0;
								}))).
				then(literal("scanCobic").
						then(argument("corner1",BlockPosArgumentType.blockPos()).
								then(argument("corner2",BlockPosArgumentType.blockPos()).
										then(argument("type",StringArgumentType.word()).suggests(sp).
												executes((ct)->{
													boolean foundAny = false;
													PointOfInterestType expectedType = Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(StringArgumentType.getString(ct, "type")));
													Iterable<BlockPos> iterator = BlockPos.iterate(BlockPosArgumentType.getLoadedBlockPos(ct, "corner1"), 
															BlockPosArgumentType.getLoadedBlockPos(ct, "corner2"));
													for(BlockPos pos:iterator) {
														if(getPoi(ct.getSource().getWorld().getPointOfInterestStorage(),pos)==expectedType) {
															foundAny = true;
															CommandUtil.feedback(ct, "Found at:"+pos.getX()+","+pos.getY()+","+pos.getZ());
														}
													}
													if(!foundAny) CommandUtil.feedback(ct, "Not Found");
													return 1;
												}))))).
				then(literal("scan").
						then(argument("center",BlockPosArgumentType.blockPos()).
								then(argument("radius",IntegerArgumentType.integer(0)).
										then(argument("type",StringArgumentType.word()).suggests(sp).
												executes((ct)->{
													PointOfInterestType expectedType = Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(StringArgumentType.getString(ct, "type")));
													Stream<PointOfInterest> poiStream = ct.getSource().getWorld().getPointOfInterestStorage().getInCircle((type)->type==expectedType, 
															BlockPosArgumentType.getLoadedBlockPos(ct, "center"), 
															IntegerArgumentType.getInteger(ct, "radius"), 
															PointOfInterestStorage.OccupationStatus.ANY);
													poiStream.forEach((poi)->{
														BlockPos pos = poi.getPos();
														CommandUtil.feedback(ct, "Found at:"+pos.getX()+","+pos.getY()+","+pos.getZ());
													});
													if(poiStream.count()==0) CommandUtil.feedback(ct, "Not Found");;
													return 1;
												}))))).
				then(literal("getDistanceToNearestOccupied").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									int distance = ct.getSource().getWorld().getOccupiedPointOfInterestDistance(ChunkSectionPos.from(BlockPosArgumentType.getLoadedBlockPos(ct, "pos")));
									CommandUtil.feedback(ct, distance);
									return 0;
								})));
		dispatcher.register(command);
	}

	private static boolean setPoi(PointOfInterestStorage poiStorage, BlockPos blockPos, String type, boolean replace) {
		if(poiStorage.getType(blockPos).isPresent()&&!replace) return false;
		poiStorage.remove(blockPos);
		poiStorage.add(blockPos, Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(type)));
		return true;
	}
	
	private static PointOfInterestType getPoi(PointOfInterestStorage poiStorage, BlockPos blockPos) {
		Optional<PointOfInterestType> type = poiStorage.getType(blockPos);
		if(type.isPresent()) {
			return type.get();
		}else {
			return null;
		}
	}
}
