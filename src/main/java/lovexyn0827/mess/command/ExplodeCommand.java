package lovexyn0827.mess.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion.DestructionType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ExplodeCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("explode").requires(CommandUtil.COMMAND_REQUMENT).
				then(argument("pos", Vec3ArgumentType.vec3()).
						then(argument("power", ExtendedFloatArgumentType.floatArg()).
								executes((ct) -> {
										ServerWorld world = ct.getSource().getWorld();
										Vec3d pos = Vec3ArgumentType.getVec3(ct, "pos");
										createExplosion(world, pos, 
												ExtendedFloatArgumentType.getFloat(ct, "power"), 
												false, null);
										CommandUtil.feedback(ct, "cmd.general.success");
										return 1;
								}).
								then(argument("fire",BoolArgumentType.bool()).
										executes((ct)->{
											ServerWorld world = ct.getSource().getWorld();
											Vec3d pos = Vec3ArgumentType.getVec3(ct, "pos");
											createExplosion(world, pos, 
													ExtendedFloatArgumentType.getFloat(ct, "power"),
													BoolArgumentType.getBool(ct, "fire"), null);
											CommandUtil.feedback(ct, "cmd.general.success");
											return 1;
										}).
										then(argument("exploder", EntityArgumentType.entity()).
												executes((ct) -> {
													ServerWorld world = ct.getSource().getWorld();
													Vec3d pos = Vec3ArgumentType.getVec3(ct, "pos");
													createExplosion(world, pos, 
															ExtendedFloatArgumentType.getFloat(ct, "power"),
															BoolArgumentType.getBool(ct, "fire"), 
															EntityArgumentType.getEntity(ct, "exploder"));
													CommandUtil.feedback(ct, "cmd.general.success");
													return 1;
												})))));
		dispatcher.register(command);
	}
	
	private static void createExplosion(ServerWorld world, Vec3d pos, float power, boolean fire, Entity exploder) {
		world.createExplosion(exploder, pos.x, pos.y, pos.z, power, fire, DestructionType.DESTROY);
	}

}
