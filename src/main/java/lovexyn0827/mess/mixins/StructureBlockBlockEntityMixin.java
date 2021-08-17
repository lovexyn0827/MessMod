package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.entity.StructureBlockBlockEntity;

@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin {
	public double getSquaredRenderDistance() {
		return 827.0D;
	}
}
