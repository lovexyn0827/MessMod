package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.FormattedText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {
	@Inject(method = "onHotbarKeyPress", 
			at = @At(value = "HEAD"), 
			cancellable = true
	)
	private static void cancelHotbarStorageOperation(MinecraftClient client, int index, boolean restore, boolean save, 
			CallbackInfo ci) {
		if(OptionManager.hotbarLength != 9) {
			client.inGameHud.setOverlayMessage(new FormattedText("misc.cancelhotbarstorage", "lc").asMutableText(), false);
			ci.cancel();
		}
	}
}
