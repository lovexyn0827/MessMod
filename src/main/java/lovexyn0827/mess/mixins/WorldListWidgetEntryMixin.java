package lovexyn0827.mess.mixins;

import java.io.File;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lovexyn0827.mess.util.FormattedText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;

@Mixin(WorldListWidget.Entry.class)
public abstract class WorldListWidgetEntryMixin {
	@Shadow
	private @Final MinecraftClient client;
	@Shadow
	private @Final LevelSummary level;
	@Shadow
	private @Final SelectWorldScreen screen;
	
	@Shadow
	protected abstract void start();
	
	@Inject(method = "play", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screen/world/WorldListWidget$Entry."
			+ "start()V"), cancellable = true)
	private void requireComfirmIfNeeded(CallbackInfo ci) {
		if(this.level.getGameMode().isSurvivalLike() 
				&& !new File(this.level.getFile().getParentFile(), "mcwmem.prop").exists()) {
			BooleanConsumer bc = (bool) -> {
				if(bool) {
					this.start();
					this.client.startIntegratedServer(this.level.getName());
				} else {
					this.client.setScreen(screen);
				}
			};
			this.client.setScreen(new ConfirmScreen(bc, new FormattedText("misc.warnsur.title", "cl").asMutableText(), 
					new FormattedText("misc.warnsur.msg", "f").asMutableText()));
			ci.cancel();
		}
	}
}
