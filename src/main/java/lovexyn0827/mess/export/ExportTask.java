package lovexyn0827.mess.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.mutable.MutableBoolean;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.rendering.RenderedBox;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;

public class ExportTask {
	private static final WorldSavePath EXPORT_PATH = WorldSavePathMixin.create("exported_saves");
	private static final Map<CommandOutput, ExportTask> TASKS = new HashMap<>();
	private final Map<String, Region> regions = new HashMap<>();
	private final MinecraftServer server;
	
	private ExportTask(MinecraftServer server) {
		this.server = server;
	}

	public static ExportTask of(CommandOutput output, MinecraftServer server) {
		return TASKS.computeIfAbsent(output, (k) -> new ExportTask(server));
	}
	
	public void addRegion(String name, ChunkPos corner1, ChunkPos corner2, ServerWorld dimension) {
		this.regions.put(name, new Region(corner1, corner2, dimension));
	}
	
	public boolean deleteRegion(String name) {
		return this.regions.remove(name) != null;
	}
	
	public void drawPreview(String name, int ticks) {
		Region region = this.regions.get(name);
		if(region != null) {
			RenderedBox box = new RenderedBox(Box.from(region.getBlockBox()), 
					0xFF0000FF, 0xFFFF003F, ticks, region.getWorld().getTime());
			MessMod.INSTANCE.shapeSender.addShape(box, region.getWorld().getRegistryKey());
		}
	}
	
	public boolean export(String name, WorldGenType wgType) throws IOException {
		Path archive = this.server.getSavePath(EXPORT_PATH);
		if(!Files.exists(archive)) {
			Files.createDirectories(archive);
		}
		
		Path temp = Files.createTempDirectory(this.server.getSavePath(EXPORT_PATH), "export_");
		this.regions.forEach((n, region) -> {
			try {
				region.export(temp);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		
		CompoundTag level = NbtIo.readCompressed(
				this.server.getSavePath(WorldSavePathMixin.create("level.dat")).toFile());
		level.getCompound("Data").putString("LevelName", name);
		CompoundTag wgConfig = level.getCompound("Data")
				.getCompound("WorldGenSettings")
				.getCompound("dimensions")
				.getCompound("minecraft:overworld");
		switch (wgType) {
		case BEDROCK:
			wgConfig.put("generator", createFlatWorld(Blocks.BEDROCK));
			break;
		case COPY:
			break;
		case GLASS:
			wgConfig.put("generator", createFlatWorld(Blocks.WHITE_STAINED_GLASS));
			break;
		case PLAIN:
			wgConfig.put("generator", createFlatWorld(Blocks.GRASS_BLOCK));
			break;
		case VOID:
			wgConfig.put("generator", createFlatWorld(Blocks.AIR));
			break;
		}
		
		NbtIo.writeCompressed(level, temp.resolve("level.dat").toFile());
		MutableBoolean success = new MutableBoolean(true);
		Path saveRoot = this.server.getSavePath(EXPORT_PATH);
		String fn = name + "-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";
		try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive.resolve(fn).toFile()))) {
			Files.walkFileTree(temp, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
					String entryPath = saveRoot.relativize(path).toString().replace('\\', '/');
					try {
						zos.putNextEntry(new ZipEntry(entryPath));
						zos.write(Files.readAllBytes(path));
					} catch (IOException e) {
						MessMod.LOGGER.warn("Failed to export: " + path.toString());
						e.printStackTrace();
						success.setFalse();
					}
					
					Files.delete(path);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			zos.finish();
		}

		return success.booleanValue();
	}

	private static CompoundTag createFlatWorld(Block block) {
		CompoundTag newConf = new CompoundTag();
		newConf.putString("type", "minecraft:flat");
		CompoundTag settings = new CompoundTag();
		CompoundTag structures = new CompoundTag();
		structures.put("structures", new CompoundTag());
		settings.put("structures", structures);
		ListTag layers = new ListTag();
		CompoundTag layer = new CompoundTag();
		layer.putString("block", Registry.BLOCK.getId(block).toString());
		layer.putInt("height", 1);
		layers.add(layer);
		settings.put("layers", layers);
		settings.putString("biome", "minecraft:the_void");
		settings.putByte("features", (byte) 0);
		settings.putByte("lakes", (byte) 0);
		newConf.put("settings", settings);
		return newConf;
	}

	public Set<String> listRegions() {
		return this.regions.keySet();
	}

	public static void reset(CommandOutput output) {
		TASKS.remove(output);
	}
}
