package lovexyn0827.mess.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.options.OptionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Redirect(method = "applyEnchantmentsToDamage", 
			at = @At(value = "FIELD", 
					target = "net/minecraft/entity/damage/DamageSource.OUT_OF_WORLD"
							+ ":Lnet/minecraft/entity/damage/DamageSource;", 
					opcode = Opcodes.GETSTATIC))
	private DamageSource redirectGetVoidDamageSource() {
		return OptionManager.resistanceReducesVoidDamage ? null : DamageSource.OUT_OF_WORLD;
	}
}
