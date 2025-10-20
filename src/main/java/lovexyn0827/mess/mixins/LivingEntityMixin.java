package lovexyn0827.mess.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.tag.TagKey;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Redirect(method = "modifyAppliedDamage", 
			at = @At(value = "INVOKE", 
					target = "net/minecraft/entity/damage/DamageSource.isIn"
							+ "(Lnet/minecraft/registry/tag/TagKey;)Z", 
					opcode = Opcodes.GETSTATIC))
	private boolean redirectGetVoidDamageSource(DamageSource source, TagKey<DamageType> tag) {
		return OptionManager.resistanceReducesVoidDamage ? false : source.isIn(tag);
	}
}
