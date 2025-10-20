package lovexyn0827.mess.mixins;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.MessModMixinPlugin;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.network.MessModPayload;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.LookingAtEntityHud;
import lovexyn0827.mess.rendering.hud.PlayerHud;
import lovexyn0827.mess.util.EntityDataDumpHelper;
import lovexyn0827.mess.util.i18n.I18N;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.text.Text;

@Mixin(value = Keyboard.class, priority = 827)
public abstract class KeyboardMixin {
	@Shadow @Final MinecraftClient client;
	
	@Inject(method = "processF3", at = @At(value = "HEAD"), cancellable = true)
	private void onF3Pressed(int key, CallbackInfoReturnable<Boolean> cir) {
		switch(key) {
		case 'E':
			LookingAtEntityHud lookingHud = MessMod.INSTANCE.getClientHudManager().lookingHud;
			if(lookingHud != null) {
				lookingHud.toggleRender();
				// This is not going to be changed, in memory of 2021...
				this.client.player.sendMessage(
						Text.literal(I18N.translate("hud.target") + (lookingHud.shouldRender ? "On" : "Off")));
			}
			
			break;
		case 'M':
			PlayerHud playerHudC = MessMod.INSTANCE.getClientHudManager().playerHudC;
			if(playerHudC != null) {
				playerHudC.toggleRender();
				this.client.player.sendMessage(
						Text.literal(I18N.translate("hud.client") + (playerHudC.shouldRender ? "On" : "Off")));
			}
			
			break;
		case 'S':
			PlayerHud playerHudS = MessMod.INSTANCE.getClientHudManager().playerHudS;
			if(playerHudS != null) {
				playerHudS.toggleRender();
				this.client.player.sendMessage(
						Text.literal(I18N.translate("hud.server") + (playerHudS.shouldRender ? "On" : "Off")));
			}
			
			break;
		default :
			return;
		}
		
		cir.setReturnValue(true);
		cir.cancel();
	}
	
	@Inject(method = "onKey", at = @At("RETURN"))
	private void handleKey(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
		boolean isBeingPressed = i == GLFW.GLFW_PRESS;
		MinecraftClient mc = MinecraftClient.getInstance();
		if(key == 'Z' && Screen.hasControlDown() && isBeingPressed) {
			if(mc.player != null) {
				mc.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(new MessModPayload(Channels.UNDO, new PacketByteBuf(Unpooled.buffer()))));
			}
		} else if(key == 'Y' && Screen.hasControlDown() && isBeingPressed) {
			if(mc.player != null) {
				mc.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(new MessModPayload(Channels.REDO, new PacketByteBuf(Unpooled.buffer()))));
			}
		} else if(key == 'C' && Screen.hasControlDown() && isBeingPressed) {
			if(OptionManager.dumpTargetEntityDataWithCtrlC && mc.player != null) {
				if(OptionManager.dumpTargetEntityDataOnClient) {
					EntityDataDumpHelper.tryDumpTarget(mc.player);
				} else {
					MessMod.INSTANCE.getClientNetworkHandler().send(
							new CustomPayloadC2SPacket(new MessModPayload(Channels.ENTITY_DUMP, new PacketByteBuf(Unpooled.buffer()))));
				}
			}
		} else if(key == GLFW.GLFW_KEY_F8 && isBeingPressed && this.client.currentScreen instanceof TitleScreen) {
			MessModMixinPlugin.tryOpenMixinChoosingFrame(null);
		}
	}
	
	private static boolean shouldPassPlayInput() {
		return OptionManager.playerInputsWhenScreenOpened
				&& InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 
						OptionManager.playerInputsWhenScreenOpenedHotkey);
	}
	
	@Redirect(
			method = { "onKey", "onChar" }, 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/client/MinecraftClient.currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
			)
	)
	private Screen shouldAlwaysHandlePlayerInputs(MinecraftClient client) {
		return shouldPassPlayInput() ? null : client.currentScreen;
	}
}
