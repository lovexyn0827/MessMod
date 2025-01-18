package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.ServerCommandSource;

public class PartlyKillCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("partlykill").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("entities", EntityArgumentType.entities())
						.then(argument("possibility", DoubleArgumentType.doubleArg(0.0D, 1.0D))
								.executes((ct) -> {
									double p = DoubleArgumentType.getDouble(ct, "possibility");
									MutableInt selected = new MutableInt();
									MutableInt killed = new MutableInt();
									EntityArgumentType.getEntities(ct, "entities").forEach((e) -> {
										selected.increment();;
										if(Math.random() < p) {
											killed.increment();
											if(OptionManager.mobFastKill && e instanceof MobEntity) {
												e.remove(RemovalReason.KILLED);
											} else {
												e.kill(ct.getSource().getWorld());
											}
										}
									});
									
									CommandUtil.feedbackWithArgs(ct, "cmd.partlykill.result", selected.getValue(), killed.getValue());
									return Command.SINGLE_SUCCESS;
								})));
		dispatcher.register(command);
	}
}
