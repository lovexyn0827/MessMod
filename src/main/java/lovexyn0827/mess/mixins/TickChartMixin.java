package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.gui.hud.debug.TickChart;

@Mixin(TickChart.class)
public class TickChartMixin {
	@ModifyConstant(
			method = "renderThresholds", 
			constant = @Constant(stringValue = "20 TPS")
	)
	private String modifyTpsChartLabel(String original) {
		double scale = OptionManager.tpsGraphScale;
		if(scale == 1.0 || !original.endsWith("TPS")) {
			return original;
		}
		
		double scaled = 20 * scale;
		if(scaled == (int) scaled) {
			return Integer.toString((int) scaled) + " TPS";
		} else {
			return Double.toString(scaled) + " TPS";
		}
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
