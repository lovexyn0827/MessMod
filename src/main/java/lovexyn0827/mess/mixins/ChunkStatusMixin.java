package lovexyn0827.mess.mixins;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.RangeParser;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
	@Inject(method = "runGenerationTask", at = @At(value = "HEAD"), cancellable = true)
	private void skipTaskIfNeeded(Executor executor, ServerWorld world, ChunkGenerator chunkGenerator, StructureManager structureManager, 
			ServerLightingProvider lightingProvider, 
			Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> chunks, boolean bl, 
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
	
	@Inject(method = "runGenerationTask", at = @At(value = "RETURN"), cancellable = true)
	private void generateChunkGrid(Executor executor, ServerWorld world, ChunkGenerator generator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> fullChunkConverter, List<Chunk> chunks, boolean bl, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
		ChunkStatus s = (ChunkStatus)(Object) this;
		if(OptionManager.generateChunkGrid && (s == ChunkStatus.SURFACE && !world.getDimension().hasCeiling()
				|| s == ChunkStatus.FEATURES && world.getDimension().hasCeiling())) {
			Chunk chunk = chunks.get(chunks.size() / 2);
			ChunkPos chunkPos = chunk.getPos();
			BlockPos start = chunkPos.getStartPos();
			int endX = start.getX() + 15;
			int endZ = start.getZ() + 15;
			BlockPos.Mutable pos = new BlockPos.Mutable();
			Heightmap heights = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
			BlockState block = ((chunkPos.x & 1) != (chunkPos.z &1)) ? 
					Blocks.PURPLE_STAINED_GLASS.getDefaultState() : Blocks.LIME_STAINED_GLASS.getDefaultState();
			for(int x = start.getX(); x <= endX; x++) {
				for(int z = start.getZ(); z <= endZ; z++) {
					pos.set(x, heights.get(x & 0xF, z & 0xF) - 1, z);
					chunk.setBlockState(pos, block, false);
				}
			}
		}
	}
}
