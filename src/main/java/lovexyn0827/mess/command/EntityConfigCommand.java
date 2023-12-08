package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.EntityInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class EntityConfigCommand {
	@SuppressWarnings("resource")
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> dsh = literal("disableStepHeight").
				executes((ct)->{
					getEntities(ct).forEach((e) -> ((EntityInterface) e).setStepHeightDisabled(true));
					CommandUtil.feedback(ct, "cmd.general.success");
					return 1;
				});
		LiteralArgumentBuilder<ServerCommandSource> esh = literal("enableStepHeight").
				executes((ct)->{
					getEntities(ct).forEach((e) -> ((EntityInterface) e).setStepHeightDisabled(false));
					CommandUtil.feedback(ct, "cmd.general.success");
					return 1;
				});	
		LiteralArgumentBuilder<ServerCommandSource> command = literal("entityconfig").
				requires(CommandUtil.COMMAND_REQUMENT).requires((s) -> !MessMod.isDedicatedEnv()).
				then(argument("targets",EntityArgumentType.entities()).then(dsh).then(esh)).
				then(literal("localPlayer").
						then(literal("disableStepHeight").
								executes((ct)->{
									((EntityInterface) MinecraftClient.getInstance().player).setStepHeightDisabled(true);;
									CommandUtil.feedback(ct, "cmd.general.success");
									return 1;
								})).
						then(literal("enableStepHeight").
								executes((ct)->{
									((EntityInterface) MinecraftClient.getInstance().player).setStepHeightDisabled(false);;
									CommandUtil.feedback(ct, "cmd.general.success");
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
		
		return Collections.emptyList();
	}
	
}
