package lovexyn0827.mess.mixins;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.EntitySelectorInterface;
import lovexyn0827.mess.fakes.EntitySelectorReaderInterface;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.NetworkSide;
import net.minecraft.predicate.NumberRange.IntRange;

@Mixin(EntitySelectorReader.class)
public class EntitySelectorReaderMixin implements EntitySelectorReaderInterface {
	@Shadow
	private Predicate<Entity> predicate;
	private IntRange idRange;
	private NetworkSide side;
	private Pattern typeRegex;
	private Pattern nameRegex;
	private Pattern classRegex;

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
			this.predicate = this.predicate.and((e) -> this.idRange.test(e.getEntityId()));
		}
		
		if(this.typeRegex != null) {
			this.predicate = this.predicate.and((e) -> {
				return this.typeRegex.matcher(EntityType.getId(e.getType()).toString()).matches();
			});
		}
		
		if(this.nameRegex != null) {
			this.predicate = this.predicate.and((e) -> {
				return this.nameRegex.matcher(e.getName().getString()).matches();
			});
		}
		
		if(this.classRegex != null) {
			this.predicate = this.predicate.and((e) -> {
				Class<?> clazz = e.getClass();
				Mapping mapping = MessMod.INSTANCE.getMapping();
				for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
					String canonicalName = mapping.namedClass(clazz.getCanonicalName());
					String simpleName = mapping.simpleNamedClass(clazz.getCanonicalName());
					if(this.classRegex.matcher(canonicalName.substring(canonicalName.lastIndexOf('.'))).matches()
							|| this.classRegex.matcher(simpleName).matches()) {
						return true;
					}
				}
				
				return false;
			});
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

	@Override
	public void setTypeRegex(Pattern typeRegex) {
		this.typeRegex = typeRegex;
	}

	@Override
	public Pattern getTypeRegex() {
		return this.typeRegex;
	}

	@Override
	public void setNameRegex(Pattern nameRegex) {
		this.nameRegex = nameRegex;
	}

	@Override
	public Pattern getNameRegex() {
		return this.nameRegex;
	}

	@Override
	public void setClassRegex(Pattern classRegex) {
		this.classRegex = classRegex;
	}

	@Override
	public Pattern getClassRegex() {
		return classRegex;
	}
}
