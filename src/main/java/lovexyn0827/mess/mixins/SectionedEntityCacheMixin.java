package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.world.entity.SectionedEntityCache;

@Mixin(value = SectionedEntityCache.class, priority = 900)
public class SectionedEntityCacheMixin {
	@ModifyConstant(
			method = { "forEachInBox" }, 
			constant = @Constant(doubleValue = 2.0)
	)
	private double modifyGetEntityExpansion(double original) {
		return OptionManager.getEntityRangeExpansion;
	}
}
