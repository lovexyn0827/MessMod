package lovexyn0827.mess.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.deobfuscating.Mapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class EntityHolder {
	final Entity entity;
	private final int entityId;;
	private final CsvWriter writer;
	private int age;
	private List<Field> listenedField = Lists.newArrayList();
	
	public EntityHolder(Entity e, EntityLogger logger) {
		this.entity = e;
		this.entityId = entity.getEntityId();
		String entityName = e.getName().asString();
		String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) 
				+ "-" + this.entityId + "-"
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
			logger.customFields.forEach((field) -> {
				if(hasField(e.getClass(), field)) {
					builder.addColumn(map.namedField(field.getName()));
					this.listenedField.add(field);
				}
			});
			
			this.writer = builder.build(w);
		} catch (IOException e1) {
			throw new IllegalStateException("Unable to create new EntityHolder: ", e1);
		}
	}
	
	private static boolean hasField(Class<?> clazz, final Field field) {
		while(clazz != Object.class) {
				try {
					if(Stream.of(clazz.getDeclaredFields()).anyMatch(field::equals)) {
						return true;
					}
				} catch (SecurityException e) {}
			clazz = clazz.getSuperclass();
		}
	
		return false;
	}

	public void tick() {
		Entity e = this.entity;
		Vec3d v = e.getVelocity();
		List<Object> obs = Lists.newArrayList(new Object[] {this.age++, e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z});
		if(e instanceof LivingEntity) {
			obs.add(((LivingEntity) e).getHealth());
		}
		
		this.listenedField.forEach((f) -> {
			try {
				f.setAccessible(true);
				obs.add(f.get(entity));
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				MessMod.LOGGER.fatal("Failed to get the value of " + f.getName() + " from " + e);
				e1.printStackTrace();
				throw new RuntimeException("", e1);
			}
		});
		this.writer.println(obs.toArray());
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
		} catch (IOException e) {
			MessMod.LOGGER.warn("Failed to close: " + this.entityId);
			e.printStackTrace();
		}
	}

	public int getId() {
		return this.entityId;
	}
}