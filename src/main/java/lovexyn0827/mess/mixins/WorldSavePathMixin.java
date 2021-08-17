package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.util.WorldSavePath;

@Mixin(WorldSavePath.class)
public interface WorldSavePathMixin {
	@Invoker(value = "<init>")
	public static WorldSavePath create(String str) {
		throw new AssertionError();
	}
}
