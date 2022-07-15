package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.block.entity.StructureBlockBlockEntity;

@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin {
	
	//getSquaredRenderDistance
	public double method_11006() {
		return 10E8D;
	}
	
	// For Debugging
	public double getSquaredRenderDistance() {
		return 10E8D;
	}
}
