package lovexyn0827.mess.log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.deobfuscating.Mapping;
import lovexyn0827.mess.mixins.WorldSavePathMixin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;

// TODO: Automatic archiving
public class EntityLogger {
	public static final Map<EntityType<?>, Class<?>> ENTITY_TYPE_TO_CLASS = Maps.newHashMap();
	private static final Pattern GERERIC_TYPE_EXTRACTOR = Pattern.compile("<([0-9a-zA-Z_.]*)>");
	private static BooleanSupplier IS_TICK_FROZEN;
	Int2ObjectMap<EntityHolder> entities = new Int2ObjectOpenHashMap<>();
	Set<Field> customFields = new HashSet<>();
	private Path logPath;

	public void tick(MinecraftServer server) throws IOException {
		if(IS_TICK_FROZEN != null && IS_TICK_FROZEN.getAsBoolean()) {
			return;
		}
		
		this.entities.values().forEach(EntityHolder::tick);
		for(Int2ObjectMap.Entry<EntityHolder> entry : this.entities.int2ObjectEntrySet()) {
			if(entry.getValue().isInvaild()) {
				try {
					this.entities.remove(entry.getIntKey()).close();
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

	public void listenToField(String field, EntityType<?> type) throws Exception {
		Int2ObjectMap<EntityHolder> temp = new Int2ObjectOpenHashMap<>(this.entities);
		this.closeAll();
		Mapping map = MessMod.INSTANCE.getMapping();
		Field f = map.getFieldFromNamed(ENTITY_TYPE_TO_CLASS.get(type), field);
		if(f != null) {
			f.setAccessible(true);
			this.customFields.add(f);
		} else {
			throw new Exception("Field " + field + " does not exist!");
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

	public void initializePath(MinecraftServer server) throws IOException {
		this.logPath = server.getSavePath(WorldSavePathMixin.create("entitylog")).toAbsolutePath();
		if(!Files.exists(this.logPath)) {
			Files.createDirectory(this.logPath);
		}
	}

	public Path getLogPath() {
		return this.logPath;
	}

	static {
		Stream.of(EntityType.class.getFields())
				.filter((f) -> Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()))
				.forEach((f) -> {
					try {
						Matcher m = GERERIC_TYPE_EXTRACTOR.matcher(f.toGenericString());
						if(m.find() && f.getType() == EntityType.class) {
							Class<?> cl = Class.forName(m.group(1));
							if(Entity.class.isAssignableFrom(cl)) {
								ENTITY_TYPE_TO_CLASS.put((EntityType<?>) f.get(null), cl);
							}
						}
					} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		if(FabricLoader.getInstance().isModLoaded("carpet")) {
			try {
				Field f = Class.forName("carpet.helpers.TickSpeed").getField("process_entities");
				IS_TICK_FROZEN = () -> {
					try {
						return !f.getBoolean(null);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
						return false;
					}
				};
			} catch (ClassNotFoundException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
				IS_TICK_FROZEN = () -> false;
			}
		} else {
			IS_TICK_FROZEN = () -> false;
		}
	}
}