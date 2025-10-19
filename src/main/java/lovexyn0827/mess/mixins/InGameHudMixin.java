package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    
	@Shadow
	private @Final MinecraftClient client;
	
	@Shadow
	abstract void renderStatusBars(DrawContext ctx);
	
	@Shadow
	abstract void renderExperienceBar(DrawContext ctx, int x);
	
	@ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 9))
	private int modifyHotbarLength(int lengthO) {
		return OptionManager.hotbarLength;
	}
	
	@ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 90))
	private int modifyHotbarHudLength(int lengthO) {
		return OptionManager.hotbarLength * 10;
	}
	
	@ModifyConstant(method = "renderHotbar", constant = @Constant(intValue = 91))
	private int modifyHotbarHudLengthP1(int lengthO) {
		return OptionManager.hotbarLength * 10 + 1;
	}
	
	@Redirect(method = "renderHotbar", 
			at = @At(value = "INVOKE", 
					target = "net/minecraft/client/gui/DrawContext.drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", 
					ordinal = 0
			)
	)
	private void drawHotbarBackground(DrawContext dc, Identifier id, int x, int y, int u, int v, int width, int height) {
		if(OptionManager.hotbarLength == 9) {
			dc.drawTexture(WIDGETS_TEXTURE, x, y, u, v, width, height);
		} else {
			int slots = OptionManager.hotbarLength;
			dc.drawTexture(WIDGETS_TEXTURE, x, y, 0, 0, 1, 22);
			dc.drawTexture(WIDGETS_TEXTURE, x + slots * 20 + 1, y, 0, 0, 1, 22);
			for(int i = 0; i < slots; i++) {
				dc.drawTexture(WIDGETS_TEXTURE, x + i * 20 + 1, y, 1, 0, 20, 22);
			}
		}
	}
	
	@Inject(method = "render", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasStatusBars()Z"
			)
	)
	private void drawSurivialHudInCreativeMode(DrawContext ctx, float tickDelta, CallbackInfo ci) {
		if (OptionManager.survivalStatusBarInCreativeMode 
				&& this.client.interactionManager.getCurrentGameMode().isCreative()) {
			this.renderStatusBars(ctx);
		}
	}
	
	@Inject(method = "render", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasExperienceBar()Z"
			), 
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void drawSurivialXpBarInCreativeMode(DrawContext ctx, float tickDelta, CallbackInfo ci, 
			Window window, TextRenderer textRenderer, int i) {
		if (OptionManager.survivalXpBarInCreativeMode 
				&& this.client.interactionManager.getCurrentGameMode().isCreative()) {
			this.renderExperienceBar(ctx, i);
		}
	}
}
