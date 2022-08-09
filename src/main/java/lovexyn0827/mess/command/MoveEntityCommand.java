package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class MoveEntityCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		SuggestionProvider<ServerCommandSource> typeSuggests = (ct, builder)->{
			builder.suggest("self").suggest("piston").suggest("player").suggest("shulker").suggest("shulkerBox");
			return builder.buildFuture();
		};
		LiteralArgumentBuilder<ServerCommandSource> command = literal("moventity").requires(CommandUtil.COMMAND_REQUMENT).
				then(argument("targets", EntityArgumentType.entities()).
						then(argument("delta", Vec3ArgumentType.vec3(false)).
								then(literal("projectile").
										executes((ct) -> {
											EntityArgumentType.getEntities(ct, "targets").forEach((entity) -> {
												Vec3d pos = entity.getPos();
												Vec3d delta = Vec3ArgumentType.getVec3(ct, "delta");
												HitResult result = ProjectileUtil.getCollision(entity, (e) -> true);
												Vec3d hit = result == null ? entity.getPos().add(delta) : result.getPos();
												entity.updatePosition(hit.x, hit.y, hit.z);
												CommandUtil.feedback(ct, entity.getPos().subtract(pos));
											});
											return 1;
										})).
								then(literal("entity").
										then(argument("type", StringArgumentType.string()).suggests(typeSuggests ).
												executes((ct) -> {
													MovementType type;
													switch(StringArgumentType.getString(ct, "type")) {
													case "self":
														type = MovementType.SELF;
														break;
													case "piston":
														type = MovementType.PISTON;
														break;
													case "player":
														type = MovementType.PLAYER;
														break;
													case "shulker":
														type = MovementType.SHULKER;
														break;
													case "shulkerBox":
														type = MovementType.SHULKER_BOX;
														break;
													default:
														type = null;
														break;
													}
													
													EntityArgumentType.getEntities(ct, "targets").forEach((entity) -> {
														Vec3d pos = entity.getPos();
														entity.move(type, Vec3ArgumentType.getVec3(ct, "delta"));
														CommandUtil.feedback(ct, entity.getPos().subtract(pos));
													});
													return 1;
												})))));
		dispatcher.register(command);
	}

}
