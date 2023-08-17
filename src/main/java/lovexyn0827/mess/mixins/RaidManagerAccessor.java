package lovexyn0827.mess.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;

@Mixin(RaidManager.class)
public interface RaidManagerAccessor {
	@Accessor("raids")
	Map<Integer, Raid> getRaids();
}
