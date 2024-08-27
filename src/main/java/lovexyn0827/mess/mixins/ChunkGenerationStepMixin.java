package lovexyn0827.mess.mixins;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.options.RangeParser;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;

@Mixin(ChunkGenerationStep.class)
public abstract class ChunkGenerationStepMixin {
	@Inject(method = "run", at = @At(value = "HEAD"), cancellable = true)
	private void runGenerationTask(ChunkGenerationContext context, 
			BoundedRegionArray<AbstractChunkHolder> boundedRegionArray, Chunk chunk, 
			CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
		if(OptionManager.skippedGenerationStages.isEmpty()) {
			return;
		}
		
		ChunkStatus s = ((ChunkGenerationStep)(Object) this).targetStatus();
		if(OptionManager.skippedGenerationStages.contains(new RangeParser.ChunkStatusRange.ChunkStatusSorter(s, s.getIndex()))) {
			if (chunk instanceof ProtoChunk protoChunk && protoChunk.getStatus().isEarlierThan(s)) {
				((ProtoChunk)chunk).setStatus(s);
			}
			
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
			cir.cancel();
		}
	}
	
	@Inject(method = "run", at = @At(value = "RETURN"), cancellable = true)
	private void generateChunkGrid(ChunkGenerationContext context, 
			BoundedRegionArray<AbstractChunkHolder> boundedRegionArray, Chunk chunk, 
			CallbackInfoReturnable<CompletableFuture<Chunk>> cir) {
		ChunkStatus s = ((ChunkGenerationStep)(Object) this).targetStatus();
		ServerWorld world = context.world();
		if(OptionManager.generateChunkGrid && (s == ChunkStatus.SURFACE && !world.getDimension().hasCeiling()
				|| s == ChunkStatus.FEATURES && world.getDimension().hasCeiling())) {
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
