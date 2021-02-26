package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtTagArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class TileEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> suggestions = (ct,builder)->{
			Registry.BLOCK_ENTITY_TYPE.forEach((type)->{
				builder.suggest(Registry.BLOCK_ENTITY_TYPE.getId(type).toString());
			});
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("tileentity").requires((source)->source.hasPermissionLevel(1)).
				then(literal("get").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									BlockEntity be = ct.getSource().getWorld().getBlockEntity(BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
									if(be==null) {
										CommandUtil.feedback(ct, "null");
										return -1;
									}
									CommandUtil.feedback(ct, "Type:"+Registry.BLOCK_ENTITY_TYPE.getId(be.getType()).getPath());
									CompoundTag tag = new CompoundTag();
									CommandUtil.feedback(ct, "Data:"+be.toTag(tag));
									return 1;
								}))).
				then(literal("set").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								then(argument("type",IdentifierArgumentType.identifier()).suggests(suggestions ).
										executes((ct)->{
											BlockPos pos = (BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
											BlockEntity be = getBlockEntity(ct);;
											ct.getSource().getWorld().setBlockEntity(pos, be);
											return 1;
										}).
										then(argument("tag",NbtTagArgumentType.nbtTag()).
												executes((ct)->{
													BlockPos pos = (BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
													BlockEntity be = getBlockEntity(ct);
													be.fromTag(ct.getSource().getWorld().getBlockState(pos), 
															(CompoundTag) NbtTagArgumentType.getTag(ct, "tag"));
													ct.getSource().getWorld().setBlockEntity(pos, be);
													return 1;
											}))))).
				then(literal("remove").
						then(argument("pos",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									ct.getSource().getWorld().removeBlockEntity(BlockPosArgumentType.getLoadedBlockPos(ct, "pos"));
									return 1;
								})));
		dispatcher.register(command);
	}

	private static BlockEntity getBlockEntity(CommandContext<ServerCommandSource> ct) {
		return Registry.BLOCK_ENTITY_TYPE.get(IdentifierArgumentType.getIdentifier(ct, "type")).instantiate();
	}
}
