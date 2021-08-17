package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.RenderedBox;
import lovexyn0827.mess.rendering.RenderedLine;
import lovexyn0827.mess.rendering.ShapeRenderer;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class RaycastCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("raycast").requires((source)->source.hasPermissionLevel(1)).
				then(literal("blocks").
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
															MessMod.INSTANCE.getShapeRenderer().addShape(
																	new RenderedBox(new Box(p), 0x00007fff, 0x00ff001f, 300), world.getRegistryKey());
															world.getBlockState(p).
																	getCollisionShape(world, p).
																	forEachBox((x1, y1, z1, x2, y2, z2) -> {
																		MessMod.INSTANCE.getShapeRenderer().addShape(
																				new RenderedBox(new Box(x1, y1, z1, x2, y2, z2).offset(p), 0xffff00ff, 0xff00ff3f, 300), world.getRegistryKey());
																	});
															return null;
														}, (c) -> null);
														MessMod.INSTANCE.getShapeRenderer().addShape(new RenderedLine(end, to, 0xff0000ff, 300), world.getRegistryKey());
														MessMod.INSTANCE.getShapeRenderer().addShape(new RenderedLine(from, end, 0x00ffffff, 300), world.getRegistryKey());
													} catch (Throwable e) {
														e.printStackTrace();
													}
													return 1;
												}))))).
				then(literal("entities").
						then(argument("from",Vec3ArgumentType.vec3()).
								then(argument("to",Vec3ArgumentType.vec3())
										.then(argument("expand", DoubleArgumentType.doubleArg()).suggests((c, b) -> b.
												suggest("1.125").
												suggest("1.25").
												suggest("1.5").
												buildFuture()).
												then(argument("excludeSender", BoolArgumentType.bool()).
														executes((ct) -> {
															raycastEntities(ct, false);
															return 1;
														}).
														then(literal("visual").
																executes((ct) -> {
																	raycastEntities(ct, true);
																	return 1;
																})))))));
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
			BlockPos p = result.getBlockPos();
			CommandUtil.feedback(ct, 
					"Pos:" + String.format("%f,%f,%f,", hit.x, hit.y, hit.z) + 
					"\nBlockPos:" + '(' + p.getX() + ',' + p.getY() + ',' + p.getZ() + ')' + 
					"\nSide:" + result.getSide());
		} else {
			CommandUtil.feedback(ct, "Missed");
		}
		return 1;
	}
	
	private static void raycastEntities(CommandContext<ServerCommandSource> ct, boolean shouldAddShapes) throws CommandSyntaxException {
		Vec3d from = Vec3ArgumentType.getVec3(ct, "from");
		Vec3d to = Vec3ArgumentType.getVec3(ct, "to");
		Entity excluded = BoolArgumentType.getBool(ct, "excludeSender") ? ct.getSource().getEntity() : null;
		Box box = new Box(from, to).expand(DoubleArgumentType.getDouble(ct, "expand"));
		RegistryKey<World> worldKey = ct.getSource().getWorld().getRegistryKey();
		ShapeRenderer renderer = MessMod.INSTANCE.shapeRenderer;
		if(shouldAddShapes)	{
			renderer.addShape(new RenderedBox(box, 0x7F7F7FFF, 0x7F7F7F3F, 300), worldKey);
		}
		
		List<Entity> entities = ct.getSource().getWorld().getOtherEntities(excluded, box);
		Iterator<Entity> itr = entities.iterator();
		Vec3d pos = to;
		Entity e = null;
		double lastDistanceSq = Double.POSITIVE_INFINITY;
		while(itr.hasNext()) {
			Entity toCheck = itr.next();
			Box checkingBox = toCheck.getBoundingBox().expand(0.30000001192092896D);
			Optional<Vec3d> opt = checkingBox.raycast(from, to);
			if(opt.isPresent() && from.squaredDistanceTo(opt.get()) < lastDistanceSq) {
				e = toCheck;
				pos = opt.get();
				CommandUtil.feedback(ct, 
						"Entity : " +  (e.hasCustomName() ? e.getCustomName() : e.getType().getTranslationKey().replaceFirst("^.+\\u002e", "")) + '('  + e.getEntityId() + ')' + 
						"\nAt : " + String.format("%f,%f,%f,", pos.x, pos.y, pos.z)
				);
			}
		}
		
		if(shouldAddShapes) {
			renderer.addShape(new RenderedLine(pos, to, 0xFF0000FF, 300), worldKey);
			renderer.addShape(new RenderedLine(from, pos, 0x00FFFFFF, 300), worldKey);
			if(e != null) {
				entities.remove(e);
				renderer.addShape(new RenderedBox(e.getBoundingBox(), 0x0000FFFF, 0, 300), worldKey);
				renderer.addShape(new RenderedBox(e.getBoundingBox().expand(0.30000001192092896D), 0xFF0000FF, 0xFF00003F, 300), worldKey);
			}
			
			entities.forEach((entity) -> {
				Box aabb = entity.getBoundingBox();
				renderer.addShape(new RenderedBox(aabb, 0x0000FFFF, 0, 300), worldKey);
				renderer.addShape(new RenderedBox(aabb.expand(0.30000001192092896D), 0x00FF00FF, 0, 300), worldKey);
			});
		}
		
		if(e == null) {
			CommandUtil.feedback(ct, "Missed");
		}
		
	}
}
