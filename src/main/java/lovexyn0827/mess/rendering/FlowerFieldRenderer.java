package lovexyn0827.mess.rendering;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class FlowerFieldRenderer {
	private static final Random RANDOM = Random.create();
	private static final Object2IntMap<FlowerBlock> COLOR_BY_FLOWER = new Object2IntOpenHashMap<>();
	private static final int DEFAULT_Y = 0;

	private final MinecraftServer server;
	
	public FlowerFieldRenderer(MinecraftServer server) {
		this.server = server;
	}

	public void tick() {
		if (!OptionManager.flowerFieldRenderer) {
			return;
		}
		
		this.server.getPlayerManager().getPlayerList().forEach(this::handlePlayer);
	}
	
	private void handlePlayer(ServerPlayerEntity player) {
		ServerWorld sw = player.getServerWorld();
		Item holding = player.getActiveItem().getItem();
		boolean restricted = holding instanceof BlockItem 
				&& ((BlockItem) holding).getBlock() instanceof FlowerBlock;
		int radius = OptionManager.flowerFieldRendererRadius;
		BlockPos center = player.getBlockPos();
		BlockPos.Mutable cur = center.mutableCopy().move(-radius, 0, -radius);
		int width = 2 * radius;
		int maxY = 0;
		Int2ObjectMap<int[]> bitmapsByLayer = new Int2ObjectOpenHashMap<>();
		boolean singleLayer = OptionManager.flowerFieldRendererSingleLayer;
		for (int dx = -radius; dx < radius; dx++) {
			for (int dz = -radius; dz < radius; dz++) {
				cur.set(center, dx, 0, dz);
				BlockPos top = sw.getTopPosition(Heightmap.Type.MOTION_BLOCKING   , cur);
				int y = top.getY();
				maxY = y > maxY ? y : maxY;
				int[] bitmap = bitmapsByLayer.computeIfAbsent(singleLayer ? DEFAULT_Y : top.getY(), 
						(i) -> new int[width * width]);
				int offset = (dz + radius) * width + dx + radius;
				FlowerBlock flower = getFlowerAt(sw, top);
				if (flower != null && (!restricted || flower.asItem() == holding)) {
					int color = COLOR_BY_FLOWER.getInt(flower) | 0xFF000000;
					bitmap[offset] = color;
				}
			}
		}
		
		if (singleLayer) {
			Vec3d origin = new Vec3d(center.getX() - radius, maxY + 0.01, center.getZ() - radius);
			int[] bitmap = bitmapsByLayer.get(DEFAULT_Y);
			MessMod.INSTANCE.shapeSender.addShape(new RenderedBitmap(bitmap, 1, width, width, 
					Direction.Axis.Y, origin, 0, sw.getTime()), sw.getRegistryKey(), player);
		} else {
			bitmapsByLayer.forEach((y, bitmap) -> {
				Vec3d origin = new Vec3d(center.getX() - radius, y + 0.01, center.getZ() - radius);
				MessMod.INSTANCE.shapeSender.addShape(new RenderedBitmap(bitmap, 1, width, width, 
						Direction.Axis.Y, origin, 0, sw.getTime()), sw.getRegistryKey(), player);
			});
		}
	}
	
	@Nullable
	private static FlowerBlock getFlowerAt(ServerWorld sw, BlockPos cur) {
		List<ConfiguredFeature<?, ?>> flowerGenerators = sw.getBiome(cur)
				.value().getGenerationSettings().getFlowerFeatures();
		if (flowerGenerators.isEmpty()) {
			return null;
		}
		
		@SuppressWarnings("rawtypes")
		ConfiguredFeature fGenConf = flowerGenerators.get(0);
		RandomPatchFeatureConfig patchConf = (RandomPatchFeatureConfig) fGenConf.config();
		FeatureConfig conf = patchConf
				.feature()
				.value()
				.feature()
				.value()
				.config();
		if (conf instanceof SimpleBlockFeatureConfig) {
			BlockStateProvider fGen = ((SimpleBlockFeatureConfig) conf).toPlace();
			synchronized (fGen) {
				Block block = fGen.get(RANDOM, cur).getBlock();
				return (block instanceof FlowerBlock) ? (FlowerBlock) block : null;
			}
		} else {
			return null;
		}
	}
	
	static {
		long count = Registries.BLOCK.stream().filter(FlowerBlock.class::isInstance).count();
		float step = 6.0F / count;
		int[] i = new int[1];
		Registries.BLOCK.stream()
				.filter(FlowerBlock.class::isInstance)
				.forEach((f) -> {
					int rgb = MathHelper.hsvToRgb(step * i[0]++, 1, 1);
					COLOR_BY_FLOWER.put((FlowerBlock) f, rgb);
				});
	}
}
