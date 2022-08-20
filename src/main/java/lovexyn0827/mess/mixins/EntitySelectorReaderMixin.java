package lovexyn0827.mess.mixins;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.fakes.EntitySelectorReaderInterface;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange.IntRange;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements EntitySelectorReaderInterface {
	@Shadow
	private Predicate<Entity> predicate;
	private IntRange idRange;

	@Override
	public void setIdRange(IntRange range) {
		this.idRange = range;
	}

	@Override
	public IntRange getIdRange() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Inject(method = "read", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/command/EntitySelectorReader;build()Lnet/minecraft/command/EntitySelector;"
			)
	)
	public void addIdConstrain(CallbackInfoReturnable<EntitySelector> cir){
		if(this.idRange != null) {
			this.predicate = this.predicate.and((e) -> this.idRange.test(e.getEntityId()));
		}
	}
}
