package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.RaycastUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(value = SpawnEggItem.class, priority = 1001)
public abstract class SpawnEggItemMixin {
	@Shadow
	protected abstract EntityType<?> getEntityType(ItemStack stack);
	
	@Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;"
					+ "Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", 
			at = @At(value = "HEAD"), 
			cancellable = true
	)
	public void mountIfNeeded(World world, PlayerEntity user, Hand hand, 
			CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if(OptionManager.quickMobMounting && user instanceof ServerPlayerEntity && user.isSneaking()) {
			ServerPlayerEntity splayer = (ServerPlayerEntity) user;
			ItemStack stack = user.getStackInHand(hand);
			Entity vehicle = RaycastUtil.getTargetEntity(splayer);
			if(vehicle != null) {
				BlockPos pos = vehicle.getBlockPos();
				Entity entity = this.getEntityType(stack)
						.spawnFromItemStack((ServerWorld)world, stack, user, pos, SpawnReason.SPAWN_EGG, false, false);
				entity.startRiding(vehicle, true);
				if (!splayer.getAbilities().creativeMode) {
					stack.decrement(1);
				}
				
				cir.setReturnValue(TypedActionResult.consume(stack));
				cir.cancel();
			}
		}
	}
}
