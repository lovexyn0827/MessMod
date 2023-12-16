package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.gui.hud.debug.TickChart;

@Mixin(TickChart.class)
public class TickChartMixin {
	@Redirect(
			method = "renderThresholds", 
			at = @At(value = "INVOKE", target = "java/lang/Float.floatValue()F")
	)
	private float modifyThreshold(Float original) {
		return original.floatValue() * OptionManager.tpsGraphScale;
	}
	
	@Redirect(
			method = "getHeight", 
			at = @At(
					value = "INVOKE", 
					target = "java/lang/Math.round(D)J"
			)
	)
	private long scaleTpsChart(double toScale) {
		return Math.round(toScale * (double) OptionManager.tpsGraphScale);
	}
}
