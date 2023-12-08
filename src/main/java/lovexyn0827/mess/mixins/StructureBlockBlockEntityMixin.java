package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;

/**
 * Loaded only when the Carpet mod is not loaded or the version is 1.4.24 or below.
 */
@Mixin(value = StructureBlockBlockEntity.class, priority = 1001)
public abstract class StructureBlockBlockEntityMixin extends BlockEntity {
	public StructureBlockBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public double getSquaredRenderDistance() {
		return OptionManager.expandedStructureBlockRenderingRange ? 10E8D : super.getSquaredRenderDistance();
	}
}
