package lovexyn0827.mess.log.entity;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import lovexyn0827.mess.log.AbstractAchivingLogger;
import lovexyn0827.mess.util.CarpetUtil;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.WrappedPath;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;

// TODO Support for EnderDragonPart, whose EntityType is not specified
public final class EntityLogger extends AbstractAchivingLogger {
	public static final Logger LOGGER = LogManager.getLogger();
	// Log files containing fields updated on the server thread.
	Map<EntityIndex, EntityHolder> serverLoggingEntries = new HashMap<>();
	// Log files containing fields updated on the client thread.
	Map<EntityIndex, EntityHolder> clientLoggingEntries = new HashMap<>();
	private Map<String, EntityLogColumn> customFields = new HashMap<>();
	private final Set<EntityType<?>> autoSubTypes = Sets.newHashSet();
	private final Set<String> autoSubNames = Sets.newHashSet();
	
	public EntityLogger(MinecraftServer server) {
		super(server);
	}

	public synchronized void serverTick() {
		if(!this.autoSubTypes.isEmpty()) {
			this.server.getWorlds().forEach((world) -> {
				this.subscribe(world.getEntitiesByType(null, (e) -> {
					return this.autoSubTypes.contains(e.getType()) || 
							this.autoSubNames.contains(e.getName().asString());
				}));
			});
		}
		
		if(CarpetUtil.isTickFrozen()) {
			return;
		}
		
		this.serverLoggingEntries.values().forEach(EntityHolder::serverTick);
		Iterator<Map.Entry<EntityIndex, EntityHolder>> itr = this.serverLoggingEntries.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry<EntityIndex, EntityHolder> entry = itr.next();
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
	
	public synchronized void clientTick() {
		this.clientLoggingEntries.values().forEach(EntityHolder::clientTick);
		Iterator<Map.Entry<EntityIndex, EntityHolder>> itr = this.clientLoggingEntries.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry<EntityIndex, EntityHolder> entry = itr.next();
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
	
	protected String getLogFolderName() {
		return "entitylog";
	}
	
	public void flushAll() {
		this.serverLoggingEntries.values().forEach(EntityHolder::flush);
		this.clientLoggingEntries.values().forEach(EntityHolder::flush);
	}

	public void closeAll() {
		this.serverLoggingEntries.values().forEach(EntityHolder::close);
		this.serverLoggingEntries.clear();
		this.clientLoggingEntries.values().forEach(EntityHolder::close);
		this.clientLoggingEntries.clear();
	}

	public void listenToField(String field, EntityType<?> type, String name, AccessingPath path, TickingPhase phase) {
		Map<EntityIndex, EntityHolder> temp = new HashMap<>(this.serverLoggingEntries);
		this.closeAll();
		if(this.customFields.containsKey(name)) {
			throw new TranslatableException("exp.dupname");
		}
		
		EntityLogColumn column;
		if ("-THIS-".equals(field)) {
			column = new WrappedPath.Phased(path, name, phase);
		} else {
			Field f = Reflection.getFieldFromNamed(Reflection.ENTITY_TYPE_TO_CLASS.get(type), field);
			if(f != null) {
				column = new ListenedField.Phased(f, path, name, phase);
			} else {
				throw new TranslatableException("exp.nofield", field, type.getName().getString());
			}
		}
		
		if(!this.customFields.containsValue(column)) {
			this.customFields.put(column.getName(), column);
		} else {
			throw new TranslatableException("exp.dupfield");
		}
		
		temp.values().forEach((h) -> {
			EntityIndex idx = new EntityIndex(h.entity);
			this.serverLoggingEntries.put(idx, new EntityHolder(h.entity, this, false));
			this.clientLoggingEntries.put(idx, new EntityHolder(h.entity, this, true));
		});
	}

	public void unlistenToField(String name) {
		Map<EntityIndex, EntityHolder> temp = new HashMap<>(this.serverLoggingEntries);
		this.closeAll();
		if(this.customFields.remove(name) == null) {
			throw new TranslatableException("exp.nofieldunlistend", name);
		}
		
		temp.values().forEach((h) -> {
			EntityIndex idx = new EntityIndex(h.entity);
			this.serverLoggingEntries.put(idx, new EntityHolder(h.entity, this, false));
			this.clientLoggingEntries.put(idx, new EntityHolder(h.entity, this, true));
		});
	}

	/**
	 * @return The number of newly subscribed entities
	 */
	public int subscribe(Collection<? extends Entity> entities) {
		this.hasCreatedAnyLog = true;
		MutableInt i = new MutableInt();
		entities.forEach((e) -> {
					EntityIndex idx = new EntityIndex(e);
					this.serverLoggingEntries.computeIfAbsent(idx, (id) -> {
						i.increment();
						return new EntityHolder(e, this, false);
					});
					this.clientLoggingEntries.computeIfAbsent(idx, (id) -> {
						return new EntityHolder(e, this, true);
					});
				});
		return i.intValue();
	}

	/**
	 * @return The number of successfully unsubscribed entities
	 */
	public int unsubscribe(Collection<? extends Entity> entities) {
		MutableInt i = new MutableInt();
		entities.stream()
				.map(EntityIndex::new)
				.map(this.serverLoggingEntries::remove)
				.filter(Predicates.notNull())
				.forEach((eh)-> {
					eh.close();
					i.increment();
				});
		entities.stream()
				.map(EntityIndex::new)
				.map(this.clientLoggingEntries::remove)
				.filter(Predicates.notNull())
				.forEach((eh)-> {
					eh.close();
				});
		return i.intValue();
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

	public Map<String, EntityLogColumn> getListenedFields() {
		return this.customFields;
	}

	public void addAutoSubName(String name) {
		this.autoSubNames.add(name);
	}

	public void removeAutoSubName(String name) {
		this.autoSubNames.remove(name);
	}
	
	public int countLoggedEntities() {
		return this.serverLoggingEntries.size();
	}
	
// TODO	public static enum SideLogStoragePolicy {
//		MIXED, 
//		SEPARATED;
//	}
	
	private static class EntityIndex {
		private final int entityId;
		private final boolean isClientSideEntity;
		
		private EntityIndex(Entity e) {
			this.entityId = e.getId();
			this.isClientSideEntity = e.world.isClient;
			
		}

		@Override
		public int hashCode() {
			return Objects.hash(entityId, isClientSideEntity);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj == null) {
				return false;
			}
			
			if (getClass() != obj.getClass()) {
				return false;
			}
			
			EntityIndex other = (EntityIndex) obj;
			return entityId == other.entityId && isClientSideEntity == other.isClientSideEntity;
		}
	}
}