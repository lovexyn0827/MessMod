package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.RangeParser;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
	@Inject(method = "runGenerationTask", at = @At(value = "HEAD"), cancellable = true)
	private void skipTaskIfNeeded(ServerWorld world, ChunkGenerator chunkGenerator, StructureManager structureManager, 
			ServerLightingProvider lightingProvider, 
			Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> chunks, 
			CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		if(OptionManager.skippedGenerationStages.isEmpty()) {
			return;
		}
		
		ChunkStatus s = (ChunkStatus)(Object) this;
		if(OptionManager.skippedGenerationStages.contains(new RangeParser.ChunkStatusRange.ChunkStatusSorter(s, s.getIndex()))) {
			Chunk chunk = chunks.get(chunks.size() / 2);
			if (chunk instanceof ProtoChunk) {
				((ProtoChunk)chunk).setStatus(s);
			}
			
			cir.setReturnValue(CompletableFuture.completedFuture(Either.left(chunk)));
			cir.cancel();
		}
	}
}
