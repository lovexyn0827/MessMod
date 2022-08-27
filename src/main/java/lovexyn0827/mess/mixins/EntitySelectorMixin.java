package lovexyn0827.mess.mixins;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.EntitySelectorInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin implements EntitySelectorInterface {
	private NetworkSide side;
	@Shadow
	private boolean includesNonPlayers;
	@Shadow
	private String playerName;
	@Shadow
	private UUID uuid;
	@Shadow
	private int limit;
	@Shadow
	private boolean localWorldOnly;
	@Shadow
	private Predicate<Entity> basePredicate;
	@Shadow
	private NumberRange.FloatRange distance;
	@Shadow
	private Function<Vec3d, Vec3d> positionOffset;
	@Shadow
	@Nullable
	private Box box;
	@Shadow
	private BiConsumer<Vec3d, List<? extends Entity>> sorter;
	@Shadow
	private boolean senderOnly;
	@Shadow
	private TypeFilter<Entity, ?> entityFilter;

	@Shadow
	private List<ServerPlayerEntity> getPlayers(ServerCommandSource serverCommandSource) {
		throw new AssertionError();
	}

	@Shadow
	private void checkSourcePermission(ServerCommandSource serverCommandSource) {
		throw new AssertionError();
	}

	@Shadow
	private boolean isLocalWorldOnly() {
		throw new AssertionError();
	}

	@Shadow
	private Predicate<Entity> getPositionPredicate(Vec3d vec3d) {
		throw new AssertionError();
	}

	@Shadow
	private <T extends Entity> List<T> getEntities(Vec3d vec3d, List<Entity> list) {
		throw new AssertionError();
	}
	
	private void appendEntitiesFromClientWorld(List<Entity> list, ClientWorld world, Vec3d vec3d, Predicate<Entity> predicate) {
		if (this.box != null) {
			list.addAll(world.getEntitiesByType(this.entityFilter, this.box.offset(vec3d), predicate));
		} else {
			Iterator<Entity> itr = world.getEntities().iterator();
			while (itr.hasNext()) {
				Entity e = itr.next();
				if(predicate.test(e)) {
					list.add(e);
				}
			}
		}
	}
	
	@Override
	public void setSide(NetworkSide side) {
		this.side = side;
	}
	
	@Inject(method = "getEntities(Lnet/minecraft/server/command/ServerCommandSource;)Ljava/util/List;", 
			at = @At("HEAD"), 
			cancellable = true
	)
	private void selectClientSideEntities(ServerCommandSource serverCommandSource, 
			CallbackInfoReturnable<List<? extends Entity>> cir) {
		if(side == NetworkSide.CLIENTBOUND && !MessMod.isDedicatedEnv()) {
			this.checkSourcePermission(serverCommandSource);
			List<Entity> result = Lists.newArrayList();
			MinecraftClient mc = MinecraftClient.getInstance();
			if(mc.world == null) {
				cir.setReturnValue(result);
				cir.cancel();
			}
			
			if (this.playerName != null) {
				mc.world.getPlayers().stream()
						.filter((p) -> p.getName().asString().equals(this.playerName))
						.findFirst()
						.ifPresent(result::add);
				cir.setReturnValue(result);
				cir.cancel();
			} else if (this.uuid != null) {
				PlayerEntity player = mc.world.getPlayerByUuid(this.uuid);
				if(player != null) {
					result.add(player);
				}
				
				cir.setReturnValue(result);
				cir.cancel();
			} else {
				Vec3d vec3d = (Vec3d)this.positionOffset.apply(serverCommandSource.getPosition());
				Predicate<Entity> predicate = this.getPositionPredicate(vec3d);
				if (this.senderOnly) {
					// @s
					Entity senderServer = serverCommandSource.getEntity();
					if(senderServer != null) {
						Entity senderClient = mc.world.getEntityById(senderServer.getId());
						if(senderClient != null) {
							result.add(senderClient);
						}
					}
					
					cir.setReturnValue(result);
					cir.cancel();
				} else {
					if (this.isLocalWorldOnly()) {
						if(serverCommandSource.getWorld().getRegistryKey() == mc.world.getRegistryKey()) {
							this.appendEntitiesFromClientWorld(result, mc.world, vec3d, predicate);
						}
					} else {
						this.appendEntitiesFromClientWorld(result, mc.world, vec3d, predicate);
					}

					cir.setReturnValue(this.getEntities(vec3d, result));
					cir.cancel();
				}
			}
		}
	}
}
