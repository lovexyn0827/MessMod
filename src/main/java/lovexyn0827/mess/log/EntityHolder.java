package lovexyn0827.mess.log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.deobfuscating.Mapping;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityHolder {
	private final Entity entity;
	private final int entityId;;
	private final CsvWriter writer;
	private int age;
	
	public EntityHolder(Entity e, EntityLogger logger) {
		this.entity = e;
		this.entityId = entity.getEntityId();
		String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) 
				+ "-" + this.entityId + "-" + e.getName().asString() + ".csv";
		try {
			FileWriter w = new FileWriter(name);
			CsvWriter.Builder builder = new CsvWriter.Builder().addColumn("tick").addColumn("x").addColumn("y").addColumn("z")
				.addColumn("vx").addColumn("vy").addColumn("vz");
			Mapping map = MessMod.INSTANCE.getMapping();
			logger.customFields.forEach((f) -> {
				builder.addColumn(map.namedField(f.getName()));
			});
			this.writer = builder.build(w);
		} catch (IOException e1) {
			throw new IllegalStateException("Unable to create new EntityHolder", e1);
		}
	}
	
	public void tick() {
		Entity e = this.entity;
		Vec3d v = e.getVelocity();
		this.writer.println(this.age ++, e.getX(), e.getY(), e.getZ(), v.x, v.y, v.z);
	}
	
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException e) {
			MessMod.LOGGER.warn("Failed to flush : " + this.entityId);
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
			MessMod.LOGGER.warn("Failed to close : " + this.entityId);
			e.printStackTrace();
		}
	}

	public int getId() {
		return this.entityId;
	}
}