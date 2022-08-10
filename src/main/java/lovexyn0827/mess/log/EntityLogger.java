package lovexyn0827.mess.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import lovexyn0827.mess.util.CarpetUtil;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;

// TODO Support for EnderDragonPart, whose EntityType is not specified
// TODO Support for specifying a TickingPhase
public final class EntityLogger {
	Int2ObjectMap<EntityHolder> entities = new Int2ObjectOpenHashMap<>();
	private Map<String, ListenedField> customFields = new HashMap<>();
	private Path logPath;
	private final Set<EntityType<?>> autoSubTypes = Sets.newHashSet();
	private long lastSessionStart;
	private boolean hasCreatedAnyLog;
	private final Set<String> autoSubNames = Sets.newHashSet();

	public void tick(MinecraftServer server) throws IOException {
		if(!this.autoSubTypes.isEmpty()) {
			server.getWorlds().forEach((world) -> {
				this.subscribe(world.getEntitiesByType(null, (e) -> {
					return this.autoSubTypes.contains(e.getType()) || this.autoSubNames.contains(e.getName().asString());
				}));
			});
		}
		
		if(CarpetUtil.isTickFrozen()) {
			return;
		}
		
		this.entities.values().forEach(EntityHolder::tick);
		Iterator<Entry<EntityHolder>> itr = this.entities.int2ObjectEntrySet().iterator();
		while(itr.hasNext()) {
			Entry<EntityHolder> entry = itr.next();
			if(entry.getValue().isInvaild()) {
				try {
					entry.getValue().close();
					itr.remove();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void flushAll() {
		this.entities.values().forEach(EntityHolder::flush);
	}

	public void closeAll() {
		this.entities.values().forEach(EntityHolder::close);
		this.entities.clear();
	}

	public void listenToField(String field, EntityType<?> type, String name, AccessingPath path) {
		Int2ObjectMap<EntityHolder> temp = new Int2ObjectOpenHashMap<>(this.entities);
		this.closeAll();
		if(this.customFields.containsKey(name)) {
			throw new TranslatableException("exp.dupname");
		}
		
		Field f = Reflection.getFieldFromNamed(Reflection.ENTITY_TYPE_TO_CLASS.get(type), field);
		if(f != null) {
			ListenedField lf = new ListenedField(f, path, name);
			if(!this.customFields.containsValue(lf)) {
				this.customFields.put(lf.getCustomName(), lf);
			} else {
				throw new TranslatableException("exp.dupfield");
			}
		} else {
			throw new TranslatableException("exp.nofield", field, type.getName().getString());
		}
		
		temp.values().forEach((h) -> this.entities.put(h.getId(), new EntityHolder(h.entity, this)));
	}

	public void unlistenToField(String name) {
		Int2ObjectMap<EntityHolder> temp = new Int2ObjectOpenHashMap<>(this.entities);
		this.closeAll();
		if(this.customFields.remove(name) == null) {
			throw new TranslatableException("exp.nofieldunlistend", name);
		}
		
		temp.values().forEach((h) -> this.entities.put(h.getId(), new EntityHolder(h.entity, this)));
	}

	/**
	 * @return The number of newly subscribed entities
	 */
	public int subscribe(Collection<? extends Entity> entities) {
		MutableInt i = new MutableInt();
		entities.stream()
				.forEach((e) -> {
					this.entities.computeIfAbsent(e.getEntityId(), (id) -> {
						i.increment();
						return new EntityHolder(e, this);
					});
				});
		return i.intValue();
	}

	/**
	 * @return The number of successfully unsubscribed entities
	 */
	@SuppressWarnings("deprecation")
	public int unsubscribe(Collection<? extends Entity> entities) {
		MutableInt i = new MutableInt();
		entities.stream()
				.map(Entity::getEntityId)
				.map(this.entities::remove)
				.filter(Predicates.notNull())
				.forEach((eh)-> {
					eh.close();
					i.increment();
				});
		return i.intValue();
	}

	public void initialize(MinecraftServer server) throws IOException {
		this.lastSessionStart = System.currentTimeMillis();
		this.logPath = server.getSavePath(WorldSavePathMixin.create("entitylog")).toAbsolutePath();
		if(!Files.exists(this.logPath)) {
			Files.createDirectory(this.logPath);
		}
	}

	public Path getLogPath() {
		return this.logPath;
	}
	
	public void archiveLogs() throws IOException {
		Path archiveDir = this.logPath.resolve("archived");
		if(!Files.exists(archiveDir)) {
			Files.createDirectory(archiveDir);
		}
		
		if(this.hasCreatedAnyLog) {
			String fn = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";
			Path archive = archiveDir.resolve(fn);
			try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive.toFile()))) {
				Files.walk(this.logPath, 1)
						.filter((f) -> f.getFileName().toString().endsWith(".csv"))
						.filter((f) -> f.toFile().lastModified() >= this.lastSessionStart)
						.forEach((f) -> {
							try {
								zos.putNextEntry(new ZipEntry(f.getFileName().toString()));
								zos.write(Files.readAllBytes(f));
								Files.delete(f);
							} catch (IOException e) {
								MessMod.LOGGER.warn("Failed to archive " + f.toString());
								e.printStackTrace();
							}
						});
				zos.finish();
			}
			
			MessMod.LOGGER.info("Archived the entity logs to " + archive.toAbsolutePath().toString());
		}
	}
	
	public void addAutoSubEntityType(EntityType<?> type) {
		this.autoSubTypes.add(type);
	}
	
	public boolean removeAutoSubEntityType(EntityType<?> type) {
		return this.autoSubTypes.remove(type);
	}
	
	public boolean shouldAutoSub(EntityType<?> type) {
		return this.autoSubTypes.contains(type);
	}
	
	public ImmutableSet<EntityType<?>> listAutoSubEntityTypes() {
		return ImmutableSet.copyOf(this.autoSubTypes);
	}

	public Map<String, ListenedField> getListenedFields() {
		return this.customFields;
	}

	public void addAutoSubName(String name) {
		this.autoSubNames.add(name);
	}

	public void removeAutoSubName(String name) {
		this.autoSubNames.remove(name);
	}
	
	public int countLoggedEntities() {
		return this.entities.size();
	}
}