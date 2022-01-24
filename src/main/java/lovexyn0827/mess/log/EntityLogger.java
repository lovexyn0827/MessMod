package lovexyn0827.mess.log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Predicates;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.deobfuscating.Mapping;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

public class EntityLogger {
	Int2ObjectMap<EntityHolder> entities = new Int2ObjectOpenHashMap<>();
	Set<Field> customFields = new HashSet<>();

	public void tick(MinecraftServer server) throws IOException {
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
	}

	public void listenToField(String field) {
		Mapping map = MessMod.INSTANCE.getMapping();
		Field f = map.getFieldFromNamed(Entity.class, field);
		this.customFields.add(f);
	}

	public void subscribe(Collection<? extends Entity> entities) {
		entities.stream()
				.map((e) -> new EntityHolder(e, this))
				.filter(Predicates.notNull())
				.forEach((e) -> this.entities.put(e.getId(), e));
	}

	@SuppressWarnings("deprecation")
	public void unsubscribe(Collection<? extends Entity> entities) {
		entities.stream()
				.map(Entity::getEntityId)
				.map(this.entities::remove)
				.filter(Predicates.notNull())
				.forEach(EntityHolder::close);
	}

}