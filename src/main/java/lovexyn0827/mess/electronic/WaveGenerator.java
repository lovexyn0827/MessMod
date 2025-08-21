package lovexyn0827.mess.electronic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class WaveGenerator {
	static final Map<RegistryKey<World>, Map<BlockPos, WaveForm>> WAVEFORMS = new HashMap<>();
	
	public static int getLevelAt(RegistryKey<World> dim, BlockPos pos) {
		if (!WAVEFORMS.containsKey(dim) || !WAVEFORMS.get(dim).containsKey(pos)) {
			return 0;
		}
		
		return WAVEFORMS.get(dim).get(pos).getCurrentLevel();
	}

	public static CompletableFuture<Suggestions> suggestDefinedPos(
			CommandContext<ServerCommandSource> ct, SuggestionsBuilder b) {
		WAVEFORMS.computeIfAbsent(ct.getSource().getWorld().getRegistryKey(), (k) -> new HashMap<>())
				.keySet().forEach((pos) -> {
					b.suggest(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()));
				});
		return b.buildFuture();
	}

	public static boolean remove(RegistryKey<World> dim, BlockPos pos) {
		if (!WAVEFORMS.containsKey(dim)) {
			return false;
		}
		
		WaveForm removed = WAVEFORMS.get(dim).remove(pos);
		if (removed != null) {
			removed.unregister();
			return true;
		} else {
			return false;
		}
	}

	public static void reset() {
		WAVEFORMS.clear();
	}

	public static void register(World targetWorld, BlockPos pos, WaveForm waveForm) {
		WAVEFORMS.computeIfAbsent(targetWorld.getRegistryKey(), (k) -> new HashMap<>()).put(pos, waveForm);
	}
}
