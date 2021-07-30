package mc.lovexyn0827.mcwmem.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import mc.lovexyn0827.mcwmem.MCWMEMod;
import mc.lovexyn0827.mcwmem.rendering.RenderedBox;
import mc.lovexyn0827.mcwmem.rendering.RenderedLine;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class RaycastCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("raycast").requires((source)->source.hasPermissionLevel(1)).
				then(argument("from",Vec3ArgumentType.vec3()).
						then(argument("to",Vec3ArgumentType.vec3()).
								executes(RaycastCommand::print).
								then(literal("visual").
										executes((ct) -> {
											print(ct);
											try {
												Vec3d from = Vec3ArgumentType.getVec3(ct, "from");
												Vec3d to = Vec3ArgumentType.getVec3(ct, "to");
												RaycastContext rct = new RaycastContext(from, to, 
														RaycastContext.ShapeType.COLLIDER, 
														RaycastContext.FluidHandling.NONE, 
														ct.getSource().getEntity());
												World world = ct.getSource().getWorld();
												BlockHitResult hit = world.raycast(rct);
												Vec3d end = hit.getType() == HitResult.Type.MISS?to : hit.getPos();
												BlockView.raycast(rct, (c, p) -> {
													MCWMEMod.INSTANCE.getShapeRenderer().addShape(
															new RenderedBox(new Box(p), 0x00007fff, 0x00ff001f, 300), world.getRegistryKey());
													world.getBlockState(p).
															getCollisionShape(world, p).
															forEachBox((x1, y1, z1, x2, y2, z2) -> {
																MCWMEMod.INSTANCE.getShapeRenderer().addShape(
																		new RenderedBox(new Box(x1, y1, z1, x2, y2, z2).offset(p), 0xffff00ff, 0xff00ff3f, 300), world.getRegistryKey());
															});
													return null;
												}, (c) -> null);
												MCWMEMod.INSTANCE.getShapeRenderer().addShape(new RenderedLine(end, to, 0xff0000ff, 300), world.getRegistryKey());
												MCWMEMod.INSTANCE.getShapeRenderer().addShape(new RenderedLine(from, end, 0x00ffffff, 300), world.getRegistryKey());
											} catch (Throwable e) {
												e.printStackTrace();
											}
											return 1;
										}))));
		dispatcher.register(command);
	}
	
	private static int print(CommandContext<ServerCommandSource> ct) throws CommandSyntaxException {
		ServerWorld world = ct.getSource().getWorld();
		RaycastContext rct = new RaycastContext(Vec3ArgumentType.getVec3(ct, "from"), 
				Vec3ArgumentType.getVec3(ct, "to"), 
				RaycastContext.ShapeType.COLLIDER, 
				RaycastContext.FluidHandling.NONE, 
				ct.getSource().getEntity());
		BlockHitResult result = world.raycast(rct);
		BlockView.raycast(rct, (c, p) -> {
			CommandUtil.feedback(ct, "Checked: " + p);
			return null;
		}, (c) -> null);
		if(result != null && result.getType() != HitResult.Type.MISS) {
			Vec3d hit = result.getPos();
			CommandUtil.feedback(ct, 
					"Pos:" + String.format("%f,%f,%f,", hit.x, hit.y, hit.z) + 
					"\nBlockPos:" + result.getBlockPos() + 
					"\nSide:" + result.getSide());
		} else {
			CommandUtil.feedback(ct, "Missed");
		}
		return 1;
	}
}
