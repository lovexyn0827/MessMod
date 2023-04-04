package lovexyn0827.mess.log.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.CsvWriter;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import lovexyn0827.mess.util.phase.ClientTickingPhase;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityHolder {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	final Entity entity;
	private final int entityId;;
	private final CsvWriter writer;
	private int age;
	private Map<EntityLogColumn, Object> listenedFields = Maps.newHashMap();
	private boolean closed;
	final boolean isClient;
	
	EntityHolder(Entity e, EntityLogger logger, boolean isClient) {
		this.entity = e;
		this.entityId = entity.getEntityId();
		this.isClient = isClient;
		String entityName = e.getName().asString();
		String name = DATE_FORMAT.format(new Date()) 
				+ "@" + this.entityId + '-' + (isClient ? 'C' : 'S') + '-'
				+ (entityName.length() == 0 ? entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "") : 
						entityName) + ".csv";
		File f = logger.getLogPath().resolve(name).toFile();
		try {
			FileWriter w = new FileWriter(f);
			CsvWriter.Builder builder = new CsvWriter.Builder().addColumn("tick").addColumn("x").addColumn("y").addColumn("z")
				.addColumn("vx").addColumn("vy").addColumn("vz");
			if(e instanceof LivingEntity) {
				builder.addColumn("health");
			}
			
			Mapping map = MessMod.INSTANCE.getMapping();
			logger.getListenedFields().values().forEach((field) -> {
				boolean sideMatched = (field.getPhase() instanceof ClientTickingPhase) == isClient;
				if(sideMatched && field.canGetFrom(e)) {
					builder.addColumn(map.namedField(field.getName()));
					this.listenedFields.put(field, ToBeReplaced.INSTANCE);
				}
			});
			
			this.writer = builder.build(w);
		} catch (IOException e1) {
			throw new TranslatableException("exp.log.holder", e1);
		}
		
		if(!MessMod.isDedicatedServerEnv() && isClient) {
			ClientTickingPhase.addEventToAll(this::updateServerData);
		} else {
			ServerTickingPhase.addEventToAll(this::updateServerData);
		}
	}

	public void serverTick() {
		if(this.isClient) {
			throw new IllegalStateException("Shouldn't be called!");
		}
		
		Entity e = this.entity;
		Vec3d v = e.getVelocity();
		List<Object> obs = Lists.newArrayList(new Object[] {this.age++, e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z});
		if(e instanceof LivingEntity) {
			obs.add(((LivingEntity) e).getHealth());
		}
		
		this.listenedFields.forEach((field, value) -> {
			if(field.getPhase() instanceof ClientTickingPhase) {
				return;
			}
			
			if(value == ToBeReplaced.INSTANCE) {
				throw new IllegalStateException("The value of " + field + " is not ready!");
			}
			
			obs.add(value);
		});
		this.listenedFields.entrySet().forEach((entry) -> entry.setValue(ToBeReplaced.INSTANCE));
		this.writer.println(obs.toArray());
	}
	
	public void clientTick() {
		if(!this.isClient) {
			throw new IllegalStateException("Shouldn't be called!");
		}
		
		Entity e = this.entity;
		Vec3d v = e.getVelocity();
		List<Object> obs = Lists.newArrayList(new Object[] {this.age++, e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z});
		if(e instanceof LivingEntity) {
			obs.add(((LivingEntity) e).getHealth());
		}
		
		this.listenedFields.forEach((field, value) -> {
			if(field.getPhase() instanceof ServerTickingPhase) {
				return;
			}
			
			if(value == ToBeReplaced.INSTANCE) {
				throw new IllegalStateException("The value of " + field + " is not ready!");
			}
			
			obs.add(value);
		});
		this.listenedFields.entrySet().forEach((entry) -> entry.setValue(ToBeReplaced.INSTANCE));
		this.writer.println(obs.toArray());
	}
	
	public void updateServerData(TickingPhase phase, World world) {
		if(this.closed) {
			return;
		}
		
		this.listenedFields.entrySet().forEach((e) -> {
			boolean isEntityWorld = e.getKey().getPhase().isNotInAnyWorld() || world == this.entity.world;
			if(e.getKey().getPhase() == phase && isEntityWorld) {
				if(e.getValue() != ToBeReplaced.INSTANCE) {
					throw new IllegalStateException("The value of " + e.getKey() + " is already set!");
				}

				e.setValue(e.getKey().getFrom(this.entity));
			}
		});
	}
	
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException e) {
			MessMod.LOGGER.warn("Failed to flush: " + this.entityId);
			e.printStackTrace();
		}
	}
	
	public boolean isInvaild() {
		return this.entity.removed;
	}

	public void close() {
		try {
			this.writer.close();
			this.closed = true;
		} catch (IOException e) {
			MessMod.LOGGER.warn("Failed to close: " + this.entityId);
			e.printStackTrace();
		}
	}

	public int getId() {
		return this.entityId;
	}
	
	// TODO Use something better
	private static enum ToBeReplaced {
		INSTANCE
	}
}