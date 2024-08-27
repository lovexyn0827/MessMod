package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	private static final Identifier HOTBAR_TEXTURE = Identifier.of("hud/hotbar");
    
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
					target = "net/minecraft/client/gui/DrawContext.drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", 
					ordinal = 0
			)
	)
	private void drawHotbarBackground(DrawContext dc, Identifier id, int x, int y, int width, int height) {
		if(OptionManager.hotbarLength == 9) {
			dc.drawGuiTexture(HOTBAR_TEXTURE, x, y, width, height);
		} else {
			int slots = OptionManager.hotbarLength;
			dc.drawTexture(HOTBAR_TEXTURE, x, y, 0, 0, 1, 22);
			dc.drawTexture(HOTBAR_TEXTURE, x + slots * 20 + 1, y, 0, 0, 1, 22);
			for(int i = 0; i < slots; i++) {
				dc.drawTexture(HOTBAR_TEXTURE, x + i * 20 + 1, y, 1, 0, 20, 22);
			}
		}
	}
}
