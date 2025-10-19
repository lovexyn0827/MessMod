package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.IdCountsState;

@Mixin(IdCountsState.class)
public interface IdCountsStateAccessor {
	@Accessor("idCounts")
	Object2IntMap<String> getIdCountsForMessMod();
}
