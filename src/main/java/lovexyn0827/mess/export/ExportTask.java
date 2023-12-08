package lovexyn0827.mess.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.mutable.MutableBoolean;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.DataCommandStorageAccessor;
import lovexyn0827.mess.mixins.MinecraftServerAccessor;
import lovexyn0827.mess.mixins.RaidManagerAccessor;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.RenderedBox;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public final class ExportTask {
	private static final WorldSavePath EXPORT_PATH = WorldSavePathMixin.create("exported_saves");
	private static final Map<CommandOutput, ExportTask> TASKS = new HashMap<>();
	private final Map<String, Region> regions = new HashMap<>();
	private final MinecraftServer server;
	private final EnumSet<SaveComponent> components = EnumSet.noneOf(SaveComponent.class);
	private final ServerPlayerEntity owner;
	
	private ExportTask(CommandOutput k, MinecraftServer server) {
		this.owner = k instanceof ServerPlayerEntity ? (ServerPlayerEntity) k : null;
		this.server = server;
		this.components.addAll(OptionManager.defaultSaveComponents);
	}

	public static ExportTask of(CommandOutput output, MinecraftServer server) {
		return TASKS.computeIfAbsent(output, (k) -> new ExportTask(k, server));
	}
	
	public void addRegion(String name, ChunkPos corner1, ChunkPos corner2, ServerWorld dimension) {
		this.regions.put(name, new Region(name, corner1, corner2, dimension));
	}
	
	public boolean deleteRegion(String name) {
		return this.regions.remove(name) != null;
	}
	
	public void drawPreview(String name, int ticks) {
		Region region = this.regions.get(name);
		if(region != null) {
			RenderedBox box = new RenderedBox(Box.from(region.getBlockBox()), 
					0xFF0000FF, 0xFFFF003F, ticks, region.getWorld().getTime());
			MessMod.INSTANCE.shapeSender.addShape(box, region.getWorld().getRegistryKey(), this.owner);
		}
	}
	
	public void addComponents(Set<SaveComponent> comps) {
		this.components.addAll(comps);
	}
	
	public void omitComponents(Set<SaveComponent> comps) {
		this.components.removeAll(comps);
	}
	
	public EnumSet<SaveComponent> getComponents() {
		return this.components;
	}
	
	public Path export(String name, WorldGenType wgType) throws IOException {
		Path archivePath = this.server.getSavePath(EXPORT_PATH);
		if(!Files.exists(archivePath)) {
			Files.createDirectories(archivePath);
		}
		
		Path temp = Files.createTempDirectory(this.server.getSavePath(EXPORT_PATH), "export_");
		this.regions.forEach((n, region) -> {
			try {
				region.export(temp, this.components);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		for(ServerWorld world : this.server.getWorlds()) {
			// Temporary dimension directory
			Path dir = temp.resolve(this.server.getSavePath(WorldSavePathMixin.create(""))
					.relativize(((MinecraftServerAccessor) this.server).getSession()
							.getWorldDirectory(world.getRegistryKey())));
			if(!dir.resolve("data").toFile().exists()) {
				Files.createDirectories(dir.resolve("data"));
			}
			
			PersistentStateManager psm = 
					new PersistentStateManager(dir.resolve("data").toFile(), this.server.getDataFixer());
			if(this.components.contains(SaveComponent.RAID)) {
				exportRaids(world, psm);
			}
			
			tryExportMaps(world, psm);
			tryExportForceChunks(world, psm);
			if(world.getRegistryKey() == World.OVERWORLD) {
				if(this.components.contains(SaveComponent.SCOREBOARD)) {
					ScoreboardState ss = new ScoreboardState(world.getScoreboard());
					psm.set("scoreboard", ss);
				}
				
				if(this.components.contains(SaveComponent.DATA_COMMAND_STORAGE)) {
					((DataCommandStorageAccessor) this.server.getDataCommandStorage()).getStorages()
							.forEach((id, cds) -> {
								cds.markDirty();
								psm.set(id, cds);
							});
				}
			}
			
			psm.save();
		}
		
		this.tryExportPlayerRelatedData(temp, "advancements", ".json", 
				SaveComponent.ADVANCEMENTS_SELF, SaveComponent.ADVANCEMENT_OTHER);
		this.tryExportPlayerRelatedData(temp, "stats", ".json", 
				SaveComponent.STAT_SELF, SaveComponent.STAT_OTHER);
		this.tryExportPlayerRelatedData(temp, "playerdata", ".dat", 
				SaveComponent.PLAYER_SELF, SaveComponent.PLAYER_OTHER);
		this.tryCopySingle(temp, "icon.png", SaveComponent.ICON);
		this.tryCopySingle(temp, "carpet.conf", SaveComponent.CARPET);
		this.tryCopySingle(temp, "mcwmem.prop", SaveComponent.MESSMOD);
		this.tryCopySingle(temp, "saved_accessing_paths.prop", SaveComponent.MESSMOD);
		this.createLevelDat(name, wgType, temp);
		return createArchive(name, archivePath, temp);
	}
	
	private void tryCopySingle(Path temp, String name, SaveComponent comp) throws IOException {
		Path origin = this.server.getSavePath(WorldSavePathMixin.create(name));
		if(this.components.contains(comp) && Files.exists(origin)) {
			Path dst = temp.resolve(name);
			Files.copy(origin, dst);
		}
	}

	private void tryExportPlayerRelatedData(Path temp, String dirName, String ext, 
			SaveComponent selfFlag, SaveComponent otherFlag) throws IOException {
		boolean copySelf = this.components.contains(selfFlag);
		boolean copyOther = this.components.contains(otherFlag);
		if(!copySelf && !copyOther) {
			return;
		}
		
		Path origin = this.server.getSavePath(WorldSavePathMixin.create(dirName));
		if(!Files.exists(origin)) {
			return;
		}
		
		Path dst = temp.resolve(dirName);
		Files.createDirectories(dst);
		for(Path p : Files.list(origin).toArray((i) -> new Path[i])) {
			if(!Files.isDirectory(p) && p.getFileName().toString().toLowerCase().endsWith(ext)) {
				UUID uuid;
				try {
					uuid = UUID.fromString(p.getFileName().toString().replace(ext, ""));
				} catch (IllegalArgumentException e) {
					continue;
				}
				
				boolean self = this.isOwner(uuid);
				if(copySelf && self || copyOther && !self) {
					Path dstFile = dst.resolve(origin.relativize(p));
					Files.copy(p, dstFile);
				}
			}
		}
	}

	private boolean isOwner(UUID uuid) {
		return this.owner == null || this.owner.getUuid().equals(uuid);
	}

	private void tryExportForceChunks(ServerWorld world, PersistentStateManager psm) {
		boolean copyLocal = this.components.contains(SaveComponent.FORCE_CHUNKS_LOCAL);
		boolean copyOther = this.components.contains(SaveComponent.FORCE_CHUNKS_OTHER);
		ForcedChunkState fcs = ForcedChunkState.fromNbt(world.getPersistentStateManager()
				.getOrCreate(ForcedChunkState::fromNbt, ForcedChunkState::new, "chunks")
				.writeNbt(new NbtCompound()));
		fcs.markDirty();
		fcs.getChunks().removeIf((LongPredicate) (pos) -> {
			boolean local = this.regions.values().stream().anyMatch((r) -> r.contains(world, pos));
			return !(local && copyLocal || !local && copyOther);
		});
	}

	private void tryExportMaps(ServerWorld world, PersistentStateManager psm) {
		boolean copyLocal = this.components.contains(SaveComponent.MAP_LOCAL);
		boolean copyOther = this.components.contains(SaveComponent.MAP_OTHER);
		int nextId = world.getNextMapId();
		for(int i = 0; i < nextId; i++) {
			String name = FilledMapItem.getMapName(i);
            MapState origin = world.getMapState(name);
			if(origin == null) {
				return;
			}
			
            MapState ms = MapState.fromNbt(origin.writeNbt(new NbtCompound()));
			ms.markDirty();
			if(ms != null) {
				boolean local = this.regions.values().stream().anyMatch((r) -> r.contains(ms));
				if(local && copyLocal || !local && copyOther) {
					psm.set(name, ms);
				}
			}
		}
		
		if((copyLocal || copyOther) && world.getRegistryKey() == World.OVERWORLD) {
			psm.set("idcounts", world.getPersistentStateManager()
			        .getOrCreate(IdCountsState::fromNbt, IdCountsState::new, "idcounts"));
		}
	}

	private Path createArchive(String name, Path archiveDir, Path temp)
			throws IOException, FileNotFoundException {
		MutableBoolean success = new MutableBoolean(true);
		for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
			name = name.replace(c, '_');
		}
		
		String escapedName = name;
		String fn = escapedName + "-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";
		Path archive = archiveDir.resolve(fn);
		try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive.toFile()))) {
			Files.walkFileTree(temp, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
					String entryPath = escapedName + '/' + temp.relativize(path).toString().replace('\\', '/');
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
		
		if(success.booleanValue()) {
			return archive;
		} else {
			return null;
		}
	}

	private void createLevelDat(String name, WorldGenType wgType, Path temp) throws IOException {
	    NbtCompound level = NbtIo.readCompressed(
				this.server.getSavePath(WorldSavePathMixin.create("level.dat")).toFile());
		level.getCompound("Data").putString("LevelName", name);
		if(!this.components.contains(SaveComponent.GAMERULES)) {
			level.getCompound("Data").put("GameRules", new GameRules().toNbt());
		}
		
		NbtCompound wgConfig = level.getCompound("Data")
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
	}

	private void exportRaids(ServerWorld world, PersistentStateManager psm) throws IOException {
	    String id = RaidManager.nameFor(world.getDimensionEntry());
		RaidManager ps = world.getPersistentStateManager()
				.get((nbt) -> RaidManager.fromNbt(world, nbt), id);
		RaidManager tempRm = RaidManager.fromNbt(world, ps.writeNbt(new NbtCompound()));
		Iterator<Map.Entry<Integer, Raid>> itr = ((RaidManagerAccessor) tempRm).getRaids().entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry<Integer, Raid> entry = itr.next();
			if(!this.regions.values().stream().anyMatch((reg) -> reg.contains(world, entry.getValue().getCenter()))) {
				itr.remove();
			}
		}
		
		psm.set(id, tempRm);
	}

	private static NbtCompound createFlatWorld(Block block) {
	    NbtCompound newConf = new NbtCompound();
		newConf.putString("type", "minecraft:flat");
		NbtCompound settings = new NbtCompound();
		NbtCompound structures = new NbtCompound();
		structures.put("structures", new NbtCompound());
		settings.put("structures", structures);
		NbtList layers = new NbtList();
		NbtCompound layer = new NbtCompound();
		layer.putString("block", Registries.BLOCK.getId(block).toString());
		layer.putInt("height", 1);
		layers.add(layer);
		settings.put("layers", layers);
		settings.putString("biome", "minecraft:the_void");
		settings.putByte("features", (byte) 0);
		settings.putByte("lakes", (byte) 0);
		newConf.put("settings", settings);
		return newConf;
	}

	public Collection<Region> listRegions() {
		return this.regions.values();
	}
	
	public Set<String> listRegionNames() {
		return this.regions.keySet();
	}

	public static void reset(CommandOutput output) {
		TASKS.remove(output);
	}
}
