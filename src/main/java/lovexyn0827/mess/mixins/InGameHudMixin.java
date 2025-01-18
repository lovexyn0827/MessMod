package lovexyn0827.mess.mixins;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	private static final Identifier HOTBAR_TEXTURE = Identifier.of("hud/hotbar");
    
	@Shadow @Final
	private LayeredDrawer layeredDrawer;
	
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
					target = "net/minecraft/client/gui/DrawContext.drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", 
					ordinal = 0
			)
	)
	private void drawHotbarBackground(DrawContext dc, Function<Identifier, RenderLayer> func, Identifier id, 
			int x, int y, int width, int height) {
		if(OptionManager.hotbarLength == 9) {
			dc.drawGuiTexture(func, HOTBAR_TEXTURE, x, y, width, height);
		} else {
			int slots = OptionManager.hotbarLength;
			dc.drawGuiTexture(func, HOTBAR_TEXTURE, x, y, 0, 0, 1, 22, 1, 22);
			dc.drawGuiTexture(func, HOTBAR_TEXTURE, x + slots * 20 + 1, y, 0, 0, 1, 22, 1, 22);
			for(int i = 0; i < slots; i++) {
				dc.drawGuiTexture(func, HOTBAR_TEXTURE, x + i * 20 + 1, y, 1, 0, 20, 22, 20, 22);
			}
		}
	}
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void addMessModHudsDrawLayer(MinecraftClient mc, CallbackInfo ci) {
		this.layeredDrawer.addLayer((dc, rtc) -> {
			if(MessMod.INSTANCE.getClientHudManager() != null) {
				if (mc == null || mc.player == null) {
					return;
				}
				
				MessMod.INSTANCE.getClientHudManager().render(mc.player, mc.getServer());
			}
		});
	}
}
