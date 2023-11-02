package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lovexyn0827.mess.export.ExportTask;
import lovexyn0827.mess.export.SaveComponent;
import lovexyn0827.mess.export.WorldGenType;
import lovexyn0827.mess.mixins.ServerCommandSourceAccessor;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;

public class ExportSaveCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> regionSuggest = (ct, b) -> {
			getExportTask(ct).listRegionNames().forEach(b::suggest);
			return b.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("exportsave").requires(CommandUtil.COMMAND_REQUMENT)
				.then(literal("addRegion")
						.then(argument("name", StringArgumentType.word())
								.then(argument("corner1", ColumnPosArgumentType.columnPos())
										.then(argument("corner2", ColumnPosArgumentType.columnPos())
												.executes(ExportSaveCommand::addRegionInCurrentDimension)
												.then(argument("dimension", DimensionArgumentType.dimension())
														.executes(ExportSaveCommand::addRegion))))))
				.then(literal("deleteRegion")
						.then(argument("name", StringArgumentType.word())
								.suggests(regionSuggest)
								.executes(ExportSaveCommand::delRegion)))
				.then(literal("preview")
						.then(argument("name", StringArgumentType.word())
								.suggests(regionSuggest)
								.then(argument("ticks", IntegerArgumentType.integer(0, 1000000))
										.executes(ExportSaveCommand::addPreview))))
				.then(literal("export")
						.then(argument("name", StringArgumentType.word())
								.then(argument("worldgen", StringArgumentType.word())
										.suggests((CommandUtil.immutableSuggestions((Object[]) WorldGenType.values())))
										.executes(ExportSaveCommand::export))))
				.then(literal("listRegions")
						.executes((ct) -> {
							ExportTask task = getExportTask(ct);
							task.listRegions().forEach((r) -> CommandUtil.feedbackRaw(ct, r));;
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("reset")
						.executes((ct) -> {
							ExportTask.reset(((ServerCommandSourceAccessor) ct.getSource()).getOutput());
							return Command.SINGLE_SUCCESS;
						}))
				.then(literal("addComponent")
						.then(argument("comp", EnumSetArgumentType.of(SaveComponent.class))
								.executes(ExportSaveCommand::addComponent)))
				.then(literal("removeComponent")
						.then(argument("comp", EnumSetArgumentType.of(SaveComponent.class))
								.executes(ExportSaveCommand::removeComponent)))
				.then(literal("listComponents")
						.executes(ExportSaveCommand::listComponents));
		dispatcher.register(command);
	}

	private static int addRegion(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		ExportTask task = getExportTask(ct);
		String name = StringArgumentType.getString(ct, "name");
		if(task.listRegionNames().contains(name)) {
			CommandUtil.error(ct, "cmd.general.dupname");
			return 0;
		}
		
		ColumnPos corner1 = ColumnPosArgumentType.getColumnPos(ct, "corner1");
		ColumnPos corner2 = ColumnPosArgumentType.getColumnPos(ct, "corner2");
		task.addRegion(name, 
				new ChunkPos(corner1.x >> 4, corner1.z >> 4), 
				new ChunkPos(corner2.x >> 4, corner2.z >> 4), 
				DimensionArgumentType.getDimensionArgument(ct, "dimension"));
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addRegionInCurrentDimension(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		ExportTask task = getExportTask(ct);
		String name = StringArgumentType.getString(ct, "name");
		if(task.listRegionNames().contains(name)) {
			CommandUtil.error(ct, "cmd.general.dupname");
			return 0;
		}
		
		ColumnPos corner1 = ColumnPosArgumentType.getColumnPos(ct, "corner1");
		ColumnPos corner2 = ColumnPosArgumentType.getColumnPos(ct, "corner2");
		task.addRegion(name, 
				new ChunkPos(corner1.x >> 4, corner1.z >> 4), 
				new ChunkPos(corner2.x >> 4, corner2.z >> 4), 
				ct.getSource().getWorld());
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}
	
	private static int delRegion(CommandContext<ServerCommandSource> ct) {
		ExportTask task = getExportTask(ct);
		String name = StringArgumentType.getString(ct, "name");
		if(task.deleteRegion(name)) {
			CommandUtil.feedback(ct, "cmd.general.success");
		} else {
			CommandUtil.errorWithArgs(ct, "cmd.general.nodef", name);
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static int addPreview(CommandContext<ServerCommandSource> ct) {
		ExportTask task = getExportTask(ct);
		task.drawPreview(StringArgumentType.getString(ct, "name"), 
				IntegerArgumentType.getInteger(ct, "ticks"));
		CommandUtil.feedback(ct, "cmd.general.success");
		return Command.SINGLE_SUCCESS;
	}

	private static int export(CommandContext<ServerCommandSource> ct) {
		long start = Util.getMeasuringTimeNano();
		ExportTask task = getExportTask(ct);
		WorldGenType wg = WorldGenType.valueOf(StringArgumentType.getString(ct, "worldgen"));
		try {
			if(!task.export(StringArgumentType.getString(ct, "name"), wg)) {
				CommandUtil.error(ct, "cmd.exportsave.failexp");
				return 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
			CommandUtil.error(ct, "cmd.exportsave.failexp");
			return 0;
		}
		
		CommandUtil.feedbackWithArgs(ct, "cmd.exportsave.success", (Util.getMeasuringTimeNano() - start) / 10E8D);
		return Command.SINGLE_SUCCESS;
	}
	
	private static ExportTask getExportTask(CommandContext<ServerCommandSource> ct) {
		return ExportTask.of(((ServerCommandSourceAccessor) ct.getSource()).getOutput(), 
				ct.getSource().getMinecraftServer());
	}
	
	private static int addComponent(CommandContext<ServerCommandSource> ct) {
		Set<SaveComponent> set = EnumSetArgumentType.<SaveComponent>getEnums(ct, "comp");
		ExportTask task = getExportTask(ct);
		task.addComponents(set);
		CommandUtil.feedbackWithArgs(ct,"cmd.exportsave.addcomp", set.size(), set);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int removeComponent(CommandContext<ServerCommandSource> ct) {
		Set<SaveComponent> set = EnumSetArgumentType.<SaveComponent>getEnums(ct, "comp");
		ExportTask task = getExportTask(ct);
		task.omitComponents(set);
		CommandUtil.feedbackWithArgs(ct,"cmd.exportsave.remcomp", set.size(), set);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int listComponents(CommandContext<ServerCommandSource> ct) {
		for(SaveComponent c : getExportTask(ct).getComponents()) {
			CommandUtil.feedbackRaw(ct, c.name());
		}
		
		return Command.SINGLE_SUCCESS;
	}
}
