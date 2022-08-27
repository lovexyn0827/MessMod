package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class EntityConfigCommand {
	private static Set<Entity> noStepHeightEntities = new HashSet<>();

	public static boolean shouldDisableStepHeight(Entity entity) {
		return noStepHeightEntities.contains(entity);
	}
	
	public static void reset() {
		noStepHeightEntities.clear();
	}

	@SuppressWarnings("resource")
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> dsh = literal("disableStepHeight").
				executes((ct)->{
					noStepHeightEntities.addAll(getEntities(ct));
					return 1;
				});
		LiteralArgumentBuilder<ServerCommandSource> esh = literal("enableStepHeight").
				executes((ct)->{
					noStepHeightEntities.removeAll(getEntities(ct));
					return 1;
				});	
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entityconfig").
				requires(CommandUtil.COMMAND_REQUMENT).requires((s) -> !MessMod.isDedicatedEnv()).
				then(argument("targets",EntityArgumentType.entities()).then(dsh).then(esh)).
				then(literal("localPlayer").
						then(literal("disableStepHeight").
								executes((ct)->{
									noStepHeightEntities.add(MinecraftClient.getInstance().player);
									return 1;
								})).
						then(literal("enableStepHeight").
								executes((ct)->{
									noStepHeightEntities.remove(MinecraftClient.getInstance().player);
									return 1;
								})));
		dispatcher.register(command);
	}

	private static Collection<? extends Entity> getEntities(CommandContext<ServerCommandSource> ct) {
		try {
			return EntityArgumentType.getEntities(ct, "targets");
		} catch (CommandSyntaxException e) {
			CommandUtil.error(ct, e.getMessage());
		}
		return null;
	}
	
}
