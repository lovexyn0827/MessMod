package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.util.BlockPlacementHistory;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {
	private @Final BlockPlacementHistory blockPlacementHistory;

	@Override
	public BlockPlacementHistory getBlockPlacementHistory() {
		return this.blockPlacementHistory;
	}
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onCreate(MinecraftServer server, ServerWorld world, GameProfile profile, 
			SyncedClientOptions clientOptions, CallbackInfo ci) {
		this.blockPlacementHistory = new BlockPlacementHistory((ServerPlayerEntity)(Object) this);
	}
}
