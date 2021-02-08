package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class BiomeCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("biome").
				then(literal("get").
						then(argument("location",BlockPosArgumentType.blockPos()).
								executes((ct)->{
									Biome biome = ct.getSource().getWorld().getBiome(BlockPosArgumentType.getBlockPos(ct, "location"));
									ct.getSource().sendFeedback(new LiteralText(ct.getSource().getMinecraftServer().getRegistryManager().get(Registry.BIOME_KEY).getId(biome).toString()), false);
									return 0;
								}))).
				then(literal("set").requires((source)->source.hasPermissionLevel(1)).
						then(argument("location",BlockPosArgumentType.blockPos()).
								then(CommandManager.argument("biome", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_BIOMES).
										executes((ct)->{
											try {
												ct.getSource().sendError(new LiteralText("Not implemented in the version"));
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
											return -1;
										}))));
		dispatcher.register(command);
	}
}