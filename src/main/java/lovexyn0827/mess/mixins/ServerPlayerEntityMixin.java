package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInterface {
	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	private @Final BlockPlacementHistory blockPlacementHistory;

	@Override
	public BlockPlacementHistory getBlockPlacementHistory() {
		return this.blockPlacementHistory;
	}
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCreate(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
		this.blockPlacementHistory = new BlockPlacementHistory((ServerPlayerEntity)(Object) this);
	}
	
	@Override
	protected void tickInVoid() {
		if (!OptionManager.creativeNoVoidDamage || !this.getAbilities().invulnerable) {
			super.tickInVoid();
		}
	}
}
