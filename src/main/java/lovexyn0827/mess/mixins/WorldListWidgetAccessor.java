package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.world.WorldListWidget;

@Mixin(WorldListWidget.class)
public interface WorldListWidgetAccessor {
	@Invoker("load")
	void invokeLoadForMessMod();
}
