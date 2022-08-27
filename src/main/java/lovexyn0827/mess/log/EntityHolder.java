package lovexyn0827.mess.log;

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
import lovexyn0827.mess.util.TickingPhase;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.deobfuscating.Mapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityHolder {
	final Entity entity;
	private final int entityId;;
	private final CsvWriter writer;
	private int age;
	private Map<EntityLogColumn, Object> listenedFields = Maps.newHashMap();
	private boolean closed;
	
	public EntityHolder(Entity e, EntityLogger logger) {
		this.entity = e;
		this.entityId = entity.getId();
		String entityName = e.getName().asString();
		String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) 
				+ "@" + this.entityId + "-"
				+ (entityName.length() == 0 ? entity.getType().getTranslationKey().replaceFirst("^.+\\u002e", "") : entityName) + ".csv";
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
				if(field.canGetFrom(e)) {
					builder.addColumn(map.namedField(field.getName()));
					this.listenedFields.put(field, ToBeReplaced.INSTANCE);
				}
			});
			
			this.writer = builder.build(w);
		} catch (IOException e1) {
			throw new TranslatableException("exp.log.holder", e1);
		}
		
		TickingPhase.addEventToAll(this::updateData);
	}

	public void tick() {
		Entity e = this.entity;
		Vec3d v = e.getVelocity();
		List<Object> obs = Lists.newArrayList(new Object[] {this.age++, e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z});
		if(e instanceof LivingEntity) {
			obs.add(((LivingEntity) e).getHealth());
		}
		
		this.listenedFields.forEach((field, value) -> {
			if(value == ToBeReplaced.INSTANCE) {
				throw new IllegalStateException();
			}
			
			obs.add(value);
		});
		this.listenedFields.entrySet().forEach((entry) -> entry.setValue(ToBeReplaced.INSTANCE));
		this.writer.println(obs.toArray());
	}
	
	public void updateData(TickingPhase phase, World world) {
		if(this.closed) {
			return;
		}
		
		this.listenedFields.entrySet().forEach((e) -> {
			boolean isEntityWorld = e.getKey().getPhase().notInAnyWorld || world == this.entity.world;
			if(e.getKey().getPhase() == phase && isEntityWorld) {
				if(e.getValue() != ToBeReplaced.INSTANCE) {
					throw new IllegalStateException();
				}
				MessMod.LOGGER.info(phase);
				MessMod.LOGGER.info(e.getValue());
//				Thread.dumpStack();
				
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
		return this.entity.isRemoved();
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
	
	private static enum ToBeReplaced {
		INSTANCE
	}
}