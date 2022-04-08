package lovexyn0827.mess.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class ModifyCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		//The command may be removed after mapping can be used in /entityfield.
		//How about reserving the command as a simplified /entityfield?
		LiteralArgumentBuilder<ServerCommandSource> command = literal("modify").requires(CommandUtil.COMMAND_REQUMENT).
				then(argument("target",EntityArgumentType.entities()).
						then(literal("x").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d pos = entity.getPos();
												entity.setPos(val, pos.y,pos.z);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("y").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d pos = entity.getPos();
												entity.setPos(pos.y, val,pos.z);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("z").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d pos = entity.getPos();
												entity.setPos(pos.x, pos.y,val);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("vx").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d vec = entity.getVelocity();
												entity.setVelocity(val, vec.y,vec.z);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("vy").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d vec = entity.getVelocity();
												entity.setVelocity(vec.x,val,vec.z);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("vz").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												Vec3d vec = entity.getVelocity();
												entity.setVelocity(vec.x,vec.y,vec.z);
											},
											(entity)->true);
											return 0;
										}))).
						then(literal("yaw").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->entity.yaw = val.floatValue(),
													(entity)->true);
											return 0;
										}))).
						then(literal("pitch").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->entity.pitch = val.floatValue(),
													(entity)->true);
											return 0;
										}))).
						then(literal("forward").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->((LivingEntity)entity).forwardSpeed = val.floatValue(),
													(entity)->entity instanceof LivingEntity);
											return 0;
										}))).
						then(literal("sideway").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->((LivingEntity)entity).sidewaysSpeed = val.floatValue(),
													(entity)->entity instanceof LivingEntity);
											return 0;
										}))).
						then(literal("upwards").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->((LivingEntity)entity).upwardSpeed = val.floatValue(),
													(entity)->entity instanceof LivingEntity);
											return 0;
										}))).
						then(literal("powerX").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->((ExplosiveProjectileEntity)entity).posX = val,
													(entity)->entity instanceof ExplosiveProjectileEntity);
											return 0;
										}))).
						then(literal("powerY").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,(entity,val)->{
												((ExplosiveProjectileEntity)entity).posY = val;
											},
											(entity)->entity instanceof ExplosiveProjectileEntity);
											return 0;
										}))).
						then(literal("powerZ").
								then(argument("val",DoubleArgumentType.doubleArg()).
										executes((ct)->{
											forEachEntity(ct,
													(entity,val)->((ExplosiveProjectileEntity)entity).posZ = val,
													(entity)->entity instanceof ExplosiveProjectileEntity);
											return 0;
										}))).
						then(literal("remove").
								executes((ct)->{
									for(Entity entity:EntityArgumentType.getEntities(ct, "target")) {
										entity.remove();
									}
									return 1;
								})));
		dispatcher.register(command);
	}
	
	public static void forEachEntity(CommandContext<ServerCommandSource> ct,
			BiConsumer<Entity,Double> operation,
			Predicate<Entity> condition) {
		try {
			int count = 0;
			int success = 0;
			for(Entity entity:EntityArgumentType.getEntities(ct, "target")) {
				++count;
				if(!condition.test(entity)) continue;
				operation.accept(entity,DoubleArgumentType.getDouble(ct, "val"));
				++success;
				
			}
			String info = count+" entities selected in total,"+success+" entities succeed to be modified";
			CommandUtil.feedback(ct, info);
		} catch (CommandSyntaxException e) {
			CommandUtil.error(ct, e.getMessage());
		}
	}
}
