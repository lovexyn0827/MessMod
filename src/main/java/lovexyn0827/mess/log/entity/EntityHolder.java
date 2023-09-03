package lovexyn0827.mess.log.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityHolder {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	final Entity entity;
	private final int entityId;
	private final CsvWriter writer;
	private int age;
	private final Map<EntityLogColumn, Object> listenedFields;
	private volatile boolean closed;
	final boolean shouldTickClient;
	final boolean shouldTickServer;
	final SideLogStoragePolicy policy;
	private boolean fresh = true;
	private final TickingPhase.Event dataUpdater;
	
	EntityHolder(Entity e, EntityLogger logger, boolean isClient, SideLogStoragePolicy policy) {
		this.entity = e;
		this.entityId = entity.getId();
		this.shouldTickServer = !isClient;
		this.shouldTickClient = isClient;
		this.policy = policy;
		this.listenedFields = Maps.newHashMap();
		try {
			FileWriter w = new FileWriter(getLogFile(e, isClient ? 'C' : 'S', logger));
			CsvWriter.Builder builder = new CsvWriter.Builder().addColumn("tick")
					.addColumn("x").addColumn("y").addColumn("z")
					.addColumn("vx").addColumn("vy").addColumn("vz");
			if(e instanceof LivingEntity) {
				builder.addColumn("health");
			}
			
			Mapping map = MessMod.INSTANCE.getMapping();
			logger.getListenedFields().values().forEach((field) -> {
				boolean sideMatched = (field.getPhase() instanceof ClientTickingPhase) && this.shouldTickClient 
						|| (field.getPhase() instanceof ServerTickingPhase) && this.shouldTickServer;
				if(sideMatched && field.canGetFrom(e)) {
					builder.addColumn(map.namedField(field.getName()));
					this.listenedFields.put(field, ToBeReplaced.INSTANCE);
				}
			});	
			this.writer = builder.build(w);
		} catch (IOException e1) {
			throw new TranslatableException("exp.log.holder", e1);
		}
		
		this.dataUpdater = this::updateData;
		if(!MessMod.isDedicatedServerEnv() && this.shouldTickClient) {
			ClientTickingPhase.addEventToAll(this.dataUpdater);
		} else if(this.shouldTickServer) {
			ServerTickingPhase.addEventToAll(this.dataUpdater);
		}
	}
	
	private EntityHolder(Entity e, EntityLogger logger, boolean isClient, 
			CsvWriter writer, List<EntityLogColumn> columns) {
		this.entity = e;
		this.entityId = entity.getId();
		this.shouldTickServer = !isClient;
		this.shouldTickClient = isClient;
		this.policy = SideLogStoragePolicy.MIXED;
		this.listenedFields = Maps.newHashMap();
		columns.forEach((c) -> this.listenedFields.put(c, ToBeReplaced.INSTANCE));
		this.writer = writer;
		this.dataUpdater = this::updateData;
		if(!MessMod.isDedicatedServerEnv() && this.shouldTickClient) {
			ClientTickingPhase.addEventToAll(this.dataUpdater);
		} else if(this.shouldTickServer) {
			ServerTickingPhase.addEventToAll(this.dataUpdater);
		}
	}
	
	private static File getLogFile(Entity e, char type, EntityLogger logger) {
		String entityName = e.getName().getString();
		String name = String.format("%s@%d-%c-%s.csv", 
				DATE_FORMAT.format(new Date()), 
				e.getId(), 
				type, 
				(entityName.length() == 0 ? e.getType()
						.getTranslationKey()
						.replaceFirst("^.+\\u002e", "") : entityName));
		return logger.getLogPath().resolve(name).toFile();
	}
	
	/**
	 * 
	 * @return Pair(Client, Server)
	 */
	static Pair<EntityHolder, EntityHolder> createMixedHolderPair(Entity e, EntityLogger logger){
		try {
			FileWriter w = new FileWriter(getLogFile(e, 'M', logger));
			CsvWriter.Builder builder = new CsvWriter.Builder().addColumn("tick")
					.addColumn("x").addColumn("y").addColumn("z")
					.addColumn("vx").addColumn("vy").addColumn("vz");
			if(e instanceof LivingEntity) {
				builder.addColumn("health");
			}
			
			Mapping map = MessMod.INSTANCE.getMapping();
			List<EntityLogColumn> columns = new ArrayList<>();
			logger.getListenedFields().values().forEach((field) -> {
				if(field.canGetFrom(e)) {
					builder.addColumn(map.namedField(field.getName()));
					columns.add(field);
				}
			});	
			CsvWriter writer = builder.build(w);
			return new Pair<>(new EntityHolder(e, logger, true, writer, columns), 
					new EntityHolder(e, logger, false, writer, columns));
		} catch (IOException e1) {
			throw new TranslatableException("exp.log.holder", e1);
		}
	}

	public void serverTick() {
		if(!this.shouldTickServer) {
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
				if(this.policy == SideLogStoragePolicy.MIXED) {
					obs.add("");
				}
				
				return;
			}
			
			if(value == ToBeReplaced.INSTANCE && !this.fresh) {
				throw new IllegalStateException("The value of " + field + " hasn't been set!");
			}
			
			obs.add(value);
		});
		this.listenedFields.entrySet().forEach((entry) -> entry.setValue(ToBeReplaced.INSTANCE));
		this.writer.println(obs.toArray());
		this.fresh = false;
	}
	
	public void clientTick() {
		if(!this.shouldTickClient) {
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
				if(this.policy == SideLogStoragePolicy.MIXED) {
					obs.add("");
				}
				
				return;
			}
			
			if(value == ToBeReplaced.INSTANCE && !this.fresh) {
				throw new IllegalStateException("The value of " + field + " hasn't been set!");
			}
			
			obs.add(value);
		});
		this.listenedFields.entrySet().forEach((entry) -> entry.setValue(ToBeReplaced.INSTANCE));
		this.writer.println(obs.toArray());
		this.fresh = false;
	}
	
	public void updateData(TickingPhase phase, World world) {
		if(this.closed) {
			return;
		}
		
		this.listenedFields.entrySet().forEach((e) -> {
			boolean isEntityWorld = e.getKey().getPhase().isNotInAnyWorld() || world == this.entity.world;
			if(e.getKey().getPhase() == phase && isEntityWorld) {
				if(e.getValue() != ToBeReplaced.INSTANCE) {
					throw new IllegalStateException("The value of " + e.getKey() + " has already been set!");
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
		return this.entity.isRemoved();
	}

	public void close() {
		try {
			this.writer.close();
			this.closed = true;
			if(this.shouldTickClient) {
				ClientTickingPhase.removeEventFromAll(this.dataUpdater);
			}
			
			if(this.shouldTickServer) {
				ServerTickingPhase.removeEventFromAll(this.dataUpdater);
			}
		} catch (IOException e) {
			MessMod.LOGGER.warn("Failed to close: " + this.entityId);
			e.printStackTrace();
		}
	}

	public int getId() {
		return this.entityId;
	}
	
	// Use something better (maybe alternative other than employing ObjectHolders doesn't exist)
	private static enum ToBeReplaced {
		INSTANCE
	}
}