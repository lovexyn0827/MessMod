package lovexyn0827.mess.mixins;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import lovexyn0827.mess.fakes.EntitySelectorInterface;
import lovexyn0827.mess.fakes.EntitySelectorReaderInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange.IntRange;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements EntitySelectorReaderInterface {
	@Shadow
	private Predicate<Entity> predicate;
	private IntRange idRange;
	private NetworkSide side;

	@Override
	public void setIdRange(IntRange range) {
		this.idRange = range;
	}

	@Override
	public IntRange getIdRange() {
		return this.idRange;
	}

	@Override
	public void setSide(NetworkSide side) {
		this.side = side;
	}

	@Override
	public NetworkSide getSide() {
		return this.side;
	}
	
	@Redirect(method = "read", 
			at = @At(value = "INVOKE", 
					target = "Lnet/minecraft/command/EntitySelectorReader;build()Lnet/minecraft/command/EntitySelector;"
			)
	)
	public EntitySelector addCustomOptions(EntitySelectorReader reader){
		if(this.idRange != null) {
			this.predicate = this.predicate.and((e) -> this.idRange.test(e.getId()));
		}

		EntitySelector selector = this.build();
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			((EntitySelectorInterface) selector).setSide(this.side == null ? NetworkSide.SERVERBOUND : this.side);
		}
		
		return selector;
	}

	@Shadow
	private EntitySelector build() {
		throw new AssertionError();
	}
}
