package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class CountEntitiesCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("countentities").requires(CommandUtil.COMMAND_REQUMENT)
				.executes((ct) -> {
					int count = 0;
					Iterator<Entity> itr = ct.getSource().getWorld().iterateEntities().iterator();
					for(;itr.hasNext();itr.next()) {
						count++;
					}
					
					CommandUtil.feedbackWithArgs(ct, "cmd.countentities.result", count);
					return Command.SINGLE_SUCCESS;
				})
				.then(argument("selector", EntityArgumentType.entities())
						.executes((ct) -> {
							Collection<?> entities = EntityArgumentType.getEntities(ct, "selector");
							CommandUtil.feedbackWithArgs(ct, "cmd.countentities.result", entities.size());
							return Command.SINGLE_SUCCESS;
						})
						.then(argument("stackedWith", EntityArgumentType.entity())
								.executes((ct) -> {
									Collection<? extends Entity> entities = 
											EntityArgumentType.getEntities(ct, "selector");
									Vec3d centerOfStack = EntityArgumentType.getEntity(ct, "stackedWith").getPos();
									int count = count(entities, centerOfStack::equals);
									
									CommandUtil.feedbackWithArgs(ct, "cmd.countentities.result", count);
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("maxDistanceVec", Vec3ArgumentType.vec3(false))
										.suggests(CommandUtil.immutableSuggestions("0.01 0.01 0.01"))
										.executes((ct) -> {
											Collection<? extends Entity> entities = 
													EntityArgumentType.getEntities(ct, "selector");
											Vec3d centerOfStack = EntityArgumentType.getEntity(ct, "stackedWith").getPos();
											Vec3d delta = Vec3ArgumentType.getVec3(ct, "maxDistanceVec");
											int count = count(entities, (p) -> {
												return Math.abs(p.x - centerOfStack.x) < delta.x && 
														Math.abs(p.y - centerOfStack.y) < delta.y && 
														Math.abs(p.z - centerOfStack.z) < delta.z;
											});
											
											CommandUtil.feedbackWithArgs(ct, "cmd.countentities.result", count);
											return Command.SINGLE_SUCCESS;
										}))
								.then(argument("maxDistance", DoubleArgumentType.doubleArg(0))
										.executes((ct) -> {
											Collection<? extends Entity> entities = 
													EntityArgumentType.getEntities(ct, "selector");
											Vec3d centerOfStack = EntityArgumentType.getEntity(ct, "stackedWith").getPos();
											double maxDistance = DoubleArgumentType.getDouble(ct, "maxDistance");
											double maxDistanceSquared = maxDistance * maxDistance;
											int count = count(entities, (p) -> {
												return p.squaredDistanceTo(centerOfStack) < maxDistanceSquared;
											});
											
											CommandUtil.feedbackWithArgs(ct, "cmd.countentities.result", count);
											return Command.SINGLE_SUCCESS;
										}))));
		dispatcher.register(command);
	}
	
	private static int count(Iterable<? extends Entity> entities, Predicate<? super Vec3d> condition) {
		int count = 0;
		for(Entity e : entities) {
			if(condition.test(e.getPos())) {
				count++;
			}
		}
		
		return count;
	}
	
}
