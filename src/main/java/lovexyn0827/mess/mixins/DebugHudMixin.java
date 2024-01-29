package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.MetricsData;

@Mixin(DebugHud.class)
public class DebugHudMixin {
	private boolean renderingFpsChart;
	
	@Inject(method = "drawMetricsData", at = @At("HEAD"))
	private void onGraphDrawingBegin(MatrixStack matrices, MetricsData metricsData, 
			int x, int width, boolean showFps, CallbackInfo ci) {
		this.renderingFpsChart = showFps;
	}
	
	@ModifyArg(
			method = "drawMetricsData", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/util/MetricsData.scaleSample(JII)I"
			), 
			index = 0
	)
	private long scaleTpsChart(long toScale) {
		if(this.renderingFpsChart) {
			return toScale;
		} else {
			return (long) (toScale * (double) OptionManager.tpsGraphScale);
		}
	}
	
	@ModifyArg(
			method = "drawMetricsData", 
			at = @At(
					value = "INVOKE", 
					target = "net/minecraft/client/font/TextRenderer.draw"
							+ "(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"
			),
			index = 1
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
}
