package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;

@Mixin(value = Mouse.class, priority = 827)
public class MouseMixin {
	private static boolean shouldPassPlayInput() {
		return OptionManager.playerInputsWhenScreenOpened
				&& InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 
						OptionManager.playerInputsWhenScreenOpenedHotkey);
	}
	
	@Redirect(
			method = { "onMouseButton", "onMouseScroll" }, 
			at = @At(
					value = "FIELD", 
					target = "net/minecraft/client/MinecraftClient.currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
			)
	)
	private Screen shouldAlwaysHandlePlayerInputs(MinecraftClient client) {
		return shouldPassPlayInput() ? null : client.currentScreen;
	}
	
	@Redirect(
			method = { "onCursorPos", "updateMouse" }, 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/client/Mouse.isCursorLocked()Z"
			)
	)
	private boolean shouldAlwaysRotateCamera(Mouse mouse) {
		return shouldPassPlayInput() ? true : mouse.isCursorLocked();
	}
	
	@Redirect(
			method = "onMouseButton", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/client/Mouse.lockCursor()V"
			)
	)
	private void preventScreenClosure(Mouse mouse) {
		if (!shouldPassPlayInput()) {
			mouse.lockCursor();
		}
	}
}
