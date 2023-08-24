package lovexyn0827.mess.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.command.DataCommandStorage;
import net.minecraft.world.PersistentState;

@Mixin(DataCommandStorage.class)
public interface DataCommandStorageAccessor {
	@Accessor("storages")
	Map<String, PersistentState> getStorages();
}
