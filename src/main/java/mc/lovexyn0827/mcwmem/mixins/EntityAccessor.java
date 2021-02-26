package mc.lovexyn0827.mcwmem.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("random")
	public Random getRamdomMCWMEM();
}
