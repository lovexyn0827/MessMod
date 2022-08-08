package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

public class RideCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("ride").requires(CommandUtil.COMMAND_REQUMENT)
				.then(argument("passengers", EntityArgumentType.entities())
						.then(argument("vehicle", EntityArgumentType.entity())
								.then(argument("force", BoolArgumentType.bool())
										.executes((ct) -> {
											Entity vehicle = EntityArgumentType.getEntity(ct, "vehicle");
											MutableInt success = new MutableInt(0);
											Collection<? extends Entity> passengers = EntityArgumentType.getEntities(ct, "passengers");
											passengers.forEach((e) -> {
												if(e.startRiding(vehicle, BoolArgumentType.getBool(ct, "force"))) {
													success.increment();
												}
											});
											
											CommandUtil.feedbackWithArgs(ct, "cmd.ride.info",passengers.size(), success.getValue());
											return Command.SINGLE_SUCCESS;
								}))));
		dispatcher.register(command);
	}
}
