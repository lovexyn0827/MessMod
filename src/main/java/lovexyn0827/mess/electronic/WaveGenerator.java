package lovexyn0827.mess.electronic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WaveGenerator {
	private final Map<RegistryKey<World>, Map<BlockPos, WaveForm>> waveforms = new HashMap<>();
	
	public int getLevelAt(RegistryKey<World> dim, BlockPos pos) {
		if (!this.waveforms.containsKey(dim) || !this.waveforms.get(dim).containsKey(pos)) {
			return 0;
		}
		
		return this.waveforms.get(dim).get(pos).getCurrentLevel();
	}

	public CompletableFuture<Suggestions> suggestDefinedPos(
			CommandContext<ServerCommandSource> ct, SuggestionsBuilder b) {
		this.waveforms.computeIfAbsent(ct.getSource().getWorld().getRegistryKey(), (k) -> new HashMap<>())
				.keySet().forEach((pos) -> {
					b.suggest(String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()));
				});
		return b.buildFuture();
	}

	public boolean remove(RegistryKey<World> dim, BlockPos pos) {
		if (!this.waveforms.containsKey(dim)) {
			return false;
		}
		
		WaveForm removed = this.waveforms.get(dim).remove(pos);
		if (removed != null) {
			removed.unregister();
			return true;
		} else {
			return false;
		}
	}

	public void register(World targetWorld, BlockPos pos, WaveForm waveForm) {
		this.waveforms.computeIfAbsent(targetWorld.getRegistryKey(), (k) -> new HashMap<>()).put(pos, waveForm);
	}
}
