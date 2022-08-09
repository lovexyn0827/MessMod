package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.StructureBlockBlockEntityRenderer;

@Mixin(StructureBlockBlockEntityRenderer.class)
public abstract class StructureBlockBlockEntityRendererMixin implements BlockEntityRenderer<StructureBlockBlockEntity> {

	@Override
	public int getRenderDistance() {
		return Integer.MAX_VALUE;
	}
}
