package lovexyn0827.mess.mixins;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.EntityDataDumpHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow @Final ClientPlayerEntity player;
	@Shadow @Final IntegratedServer server;
	@Shadow @Final HitResult crosshairTarget;
	@Shadow private int itemUseCooldown;
	EntityHitResult crossHairTargetForCommandSuggestions;	// FIXME: Unused
	
	@Shadow abstract Window getWindow();
	
	@Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=blit"))
	private void onRender(boolean tick,CallbackInfo ci) {
		MessMod.INSTANCE.onRender(this.player, this.server);
	}
	
	@Inject(method = "tick", at = @At(value = "HEAD"))
	private void onTickStart(CallbackInfo ci) {
		MessMod.INSTANCE.onClientTickStart();
	}
	
	@Inject(method = "tick", at = @At(value = "RETURN"))
	private void onTickEnd(CallbackInfo ci) {
		MessMod.INSTANCE.onClientTicked();
	}

	@Inject(
			method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", 
			at = @At(value = "HEAD"))
	private void onDisconnected(Screen screen, CallbackInfo ci) {
		MessMod.INSTANCE.onDisconnected();
	}
	
	@Redirect(
			method = "render",
			at = @At(value = "INVOKE", 
					target = "Ljava/lang/Math;min(II)I")
			)
	private int modifyMaxTickPerFrame(int i, int j) {
		return Math.min(OptionManager.maxClientTicksPerFrame, j);
	}
	
	@Inject(
			method = "doAttack", 
			at = @At(value = "INVOKE", 
					target = "net/minecraft/client/network/ClientPlayerInteractionManager.attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"
			), 
			cancellable = true
	)
	private void preventAttackingInvalidEntitiesWhenNeeded(CallbackInfo ci) {
		Entity e = ((EntityHitResult)this.crosshairTarget).getEntity();
		if(OptionManager.allowTargetingSpecialEntities && (e instanceof ItemEntity 
				|| e instanceof ExperienceOrbEntity || e instanceof PersistentProjectileEntity)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
	private void onItemUsage(CallbackInfo ci) {
		if(OptionManager.dumpTargetEntityDataWithPaper && this.player != null 
				&& this.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.PAPER) {
			if(this.itemUseCooldown == 0) {
				if(OptionManager.dumpTargetEntityDataOnClient) {
					EntityDataDumpHelper.tryDumpTarget(this.player);
				} else {
					MessMod.INSTANCE.getClientNetworkHandler().send(
							new CustomPayloadC2SPacket(Channels.ENTITY_DUMP, new PacketByteBuf(Unpooled.buffer())));
				}
				
				this.itemUseCooldown = 4;
				ci.cancel();
			}
		}
	}
}