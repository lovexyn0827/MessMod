package lovexyn0827.mess.mixins;

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

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldListWidgetEntryMixin {
	@Shadow
	private @Final MinecraftClient client;
	@Shadow
	private @Final LevelSummary level;
	@Shadow
	private @Final SelectWorldScreen screen;
	@Shadow(remap = false)
	private @Final WorldListWidget field_19135;
	
	@Inject(method = "play", at = @At(
					value = "INVOKE", 
					target = "net/minecraft/client/MinecraftClient"
							+ ".createIntegratedServerLoader()Lnet/minecraft/server/integrated/IntegratedServerLoader;"
			), 
			cancellable = true
	)
	private void requireComfirmIfNeeded(CallbackInfo ci) {
		if(this.level.getGameMode().isSurvivalLike() 
				&& !this.level.getIconPath().getParent().resolve("mcwmem.prop").toFile().exists()) {
			BooleanConsumer bc = (bool) -> {
				if(bool) {
					this.client.createIntegratedServerLoader().start(this.level.getName(), () -> {
						((WorldListWidgetAccessor) this.field_19135).invokeLoadForMessMod();
						this.client.setScreen(this.screen);
					});
				} else {
					this.client.setScreen(this.screen);
				}
			};
			this.client.setScreen(new ConfirmScreen(bc, new FormattedText("misc.warnsur.title", "cl").asMutableText(), 
					new FormattedText("misc.warnsur.msg", "f").asMutableText()));
			ci.cancel();
		}
	}
}
