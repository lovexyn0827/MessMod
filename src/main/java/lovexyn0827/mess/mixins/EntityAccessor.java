package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("random")
	public Random getRamdomMCWMEM();
}
