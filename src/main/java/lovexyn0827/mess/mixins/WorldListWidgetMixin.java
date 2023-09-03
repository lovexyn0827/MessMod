package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.world.level.storage.LevelSummary;

@SuppressWarnings("rawtypes")
@Mixin(WorldListWidget.class)
public abstract class WorldListWidgetMixin extends EntryListWidget {
	public WorldListWidgetMixin(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
		super(client, itemHeight, itemHeight, itemHeight, itemHeight, itemHeight);
	}
	
	@SuppressWarnings("unchecked")
	@Redirect(method = "showSummaries", 
			at = @At(value = "INVOKE", 
					target = "net/minecraft/client/gui/screen/world/WorldListWidget"
							+ ".addEntry(Lnet/minecraft/client/gui/widget/EntryListWidget$Entry;)I"
			)
	)
	private int hideSuvivalSaves(WorldListWidget list, EntryListWidget.Entry entry) {
		LevelSummary summary = ((WorldListWidgetEntryAccessor) entry).getLevelSummary();
		if(!OptionManager.hideSurvivalSaves || (summary.hasCheats() && !summary.getGameMode().isSurvivalLike())) {
			return this.addEntry(entry);
		}
		
		return 0;
	}
}
