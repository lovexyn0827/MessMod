package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;

@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin extends BlockEntity {
	public StructureBlockBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public double getSquaredRenderDistance() {
		return 10E8D;
	}
}
