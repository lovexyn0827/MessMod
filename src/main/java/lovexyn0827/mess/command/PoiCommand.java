package lovexyn0827.mess.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.RenderedBox;
import lovexyn0827.mess.rendering.ShapeSender;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PoiCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> sp = (ct,builder)->{
			for(PointOfInterestType poi:Registry.POINT_OF_INTEREST_TYPE) {
				builder = builder.suggest(poi.toString());
			}
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("poi").requires(CommandUtil.COMMAND_REQUMENT).
				then(literal("set").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								then(argument("type",StringArgumentType.word()).suggests(sp).
										then(argument("replace",BoolArgumentType.bool()).
												executes((ct) -> {
													BlockPos pos = BlockPosArgumentType.getBlockPos(ct, "pos");
													if(setPoi(ct.getSource().getWorld().getPointOfInterestStorage(),
															pos, StringArgumentType.getString(ct, "type"), 
															BoolArgumentType.getBool(ct, "replace"))) {
														CommandUtil.feedbackWithArgs(ct, "cmd.poi.modify", pos.getX(), pos.getY(), pos.getZ());
														return 1;
													} else {
														CommandUtil.errorWithArgs(ct, "cmd.poi.existed", pos.getX(), pos.getY(), pos.getZ());
														return 0;
													}
												}))))).
				then(literal("get").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct) -> {
									PointOfInterestType type = getPoi(ct.getSource().getWorld().getPointOfInterestStorage(),
											BlockPosArgumentType.getBlockPos(ct, "pos"));
									CommandUtil.feedback(ct, type==null ? "null" : type.toString());
									return 0;
								}))).
				then(literal("scanCobic").
						then(argument("corner1",BlockPosArgumentType.blockPos()).
								then(argument("corner2",BlockPosArgumentType.blockPos()).
										then(argument("type",StringArgumentType.word()).suggests(sp).
												executes((ct) -> {
													boolean foundAny = false;
													PointOfInterestType expectedType = Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(StringArgumentType.getString(ct, "type")));
													Iterable<BlockPos> iterator = BlockPos.iterate(BlockPosArgumentType.getLoadedBlockPos(ct, "corner1"), 
															BlockPosArgumentType.getLoadedBlockPos(ct, "corner2"));
													for(BlockPos pos : iterator) {
														if(getPoi(ct.getSource().getWorld().getPointOfInterestStorage(), pos) == expectedType) {
															foundAny = true;
															CommandUtil.feedbackWithArgs(ct, "cmd.general.found", pos.getX(), pos.getY(), pos.getZ());
														}
													}
													
													if(!foundAny) {
														CommandUtil.feedback(ct, "cmd.general.notfound");
													}
													
													return 1;
												}))))).
				then(literal("scan").
						then(argument("center",BlockPosArgumentType.blockPos()).
								then(argument("radius",IntegerArgumentType.integer(0)).
										then(argument("type",StringArgumentType.word()).suggests(sp).
												executes((ct) -> forEachPoi(ct, (poi)->{
													BlockPos pos = poi.getPos();
													CommandUtil.feedbackWithArgs(ct, "cmd.general.found", pos.getX(), pos.getY(), pos.getZ());
												})))))).
				then(literal("getDistanceToNearestOccupied").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct) -> {
									int distance = ct.getSource().getWorld().getOccupiedPointOfInterestDistance(ChunkSectionPos.from(BlockPosArgumentType.getLoadedBlockPos(ct, "pos")));
									CommandUtil.feedbackRaw(ct, distance);
									return 0;
								}))).
				then(literal("visualize").
						then(argument("center",BlockPosArgumentType.blockPos()).
								then(argument("radius",IntegerArgumentType.integer(0)).
										then(argument("type",StringArgumentType.word()).suggests(sp).
												executes((ct) -> forEachPoi(ct, (poi) -> visualize(poi, ct.getSource())))))));
		dispatcher.register(command);
	}

	private static int forEachPoi(CommandContext<ServerCommandSource> ct, Consumer<PointOfInterest> action) throws CommandSyntaxException {
		PointOfInterestType expectedType = Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(StringArgumentType.getString(ct, "type")));
		List<PointOfInterest> poiList = ct.getSource()
				.getWorld()
				.getPointOfInterestStorage()
				.getInCircle((type) -> type == expectedType, 
						BlockPosArgumentType.getLoadedBlockPos(ct, "center"), 
						IntegerArgumentType.getInteger(ct, "radius"), 
						PointOfInterestStorage.OccupationStatus.ANY)
				.collect(Collectors.toList());
		if(poiList.size() == 0) {
			CommandUtil.feedback(ct, "cmd.general.notfound");
			return 0;
		}
		
		poiList.forEach(action);
		return Command.SINGLE_SUCCESS;
	}

	private static boolean setPoi(PointOfInterestStorage poiStorage, BlockPos blockPos, String type, boolean replace) {
		if(poiStorage.getType(blockPos).isPresent() && !replace) return false;
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
	
	private static void visualize(PointOfInterest poi, ServerCommandSource source) {
		ShapeSender ss = MessMod.INSTANCE.shapeSender;
		BlockPos pos = poi.getPos();
		long time = source.getWorld().getTime();
		RegistryKey<World> key = source.getWorld().getRegistryKey();
		ss.addShape(new RenderedBox(new Box(pos).expand(0.02), poi.isOccupied() ? 0xFF0000FF : 0x69604EFF , 0x4E9A6960, 300, time), 
				key, source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null);
	}
}
