package mc.lovexyn0827.mcwmem.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion.DestructionType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ExplodeCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		System.out.println("Regeristing /explode");
		LiteralArgumentBuilder<ServerCommandSource> command = literal("explode").
				then(argument("pos",Vec3ArgumentType.vec3()).
						then(argument("power",FloatArgumentType.floatArg(0)).
								executes((ct)->{
										ServerWorld world = ct.getSource().getWorld();
										Vec3d pos = Vec3ArgumentType.getVec3(ct,"pos");
										world.createExplosion(null, pos.x,pos.y,pos.z,FloatArgumentType.getFloat(ct,"power"), DestructionType.DESTROY);
										return 0;
								})));
		dispatcher.register(command);
	}

}
