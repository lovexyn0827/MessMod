package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawContext {
	public InGameHudMixin(MinecraftClient client, Immediate vertexConsumers) {
		super(client, vertexConsumers);
	}

	@Shadow
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
    
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
					target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", 
					ordinal = 0
			)
	)
	private void drawHotbarBackground(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
		if(OptionManager.hotbarLength == 9) {
			drawTexture(WIDGETS_TEXTURE, x, y, u, v, width, height);
		} else {
			int slots = OptionManager.hotbarLength;
			drawTexture(WIDGETS_TEXTURE, x, y, 0, 0, 1, 22);
			drawTexture(WIDGETS_TEXTURE, x + slots * 20 + 1, y, 0, 0, 1, 22);
			for(int i = 0; i < slots; i++) {
				drawTexture(WIDGETS_TEXTURE, x + i * 20 + 1, y, 1, 0, 20, 22);
			}
		}
	}
}
