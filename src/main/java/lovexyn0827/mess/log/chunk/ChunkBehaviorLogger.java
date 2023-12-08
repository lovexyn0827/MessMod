package lovexyn0827.mess.log.chunk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.AbstractAchivingLogger;
import lovexyn0827.mess.log.CsvWriter;
import lovexyn0827.mess.util.access.AccessingFailureException;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public final class ChunkBehaviorLogger extends AbstractAchivingLogger {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	public static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private CsvWriter currentLog;
	private final HashSet<ChunkEvent> subscribedEvents = new HashSet<>();
	private boolean working = false;
	private final LinkedHashMap<String, AccessingPath> customColumns = new LinkedHashMap<>();

	public ChunkBehaviorLogger(MinecraftServer server) {
		super(server);
	}
	
	public void start() throws IOException {
		File logFile = this.getLogPath().resolve(DATE_FORMAT.format(new Date()) + ".csv").toFile();
		FileWriter writer = new FileWriter(logFile);
		CsvWriter.Builder csvBuilder = new CsvWriter.Builder();
		csvBuilder
				.addColumn("Event")
				.addColumn("Pos")
				.addColumn("Dimension")
				.addColumn("GameTime")
				.addColumn("RealTime")
				.addColumn("Thread")
				.addColumn("Cause")
				.addColumn("Addition");
		this.customColumns.keySet().forEach(csvBuilder::addColumn);
		this.currentLog = csvBuilder.build(writer);
		this.working = true;
		this.hasCreatedAnyLog = true;
	}
	
	public void stop() throws IOException {
		this.working = false;
		this.currentLog.close();
		this.currentLog = null;
	}
	
	public void subscribeAll(Set<ChunkEvent> events) {
		this.subscribedEvents.addAll(events);
	}
	
	public void unsubscribeAll(Set<ChunkEvent> events) {
		this.subscribedEvents.removeAll(events);
	}
	
	public synchronized void onEvent(ChunkEvent event, long pos, Identifier dim, Thread thread, 
			Object cause, Object addition) {
		if(this.working && this.subscribedEvents.contains(event)) {
			if(this.customColumns.isEmpty()) {
				this.currentLog.println(event.name(), 
						pos == ChunkPos.MARKER ? null : new ChunkPos(pos), 
						dim, 
						this.server.getOverworld().getTime(), 
						Util.getMeasuringTimeNano(), 
						thread.getName(), 
						cause, 
						addition);
			} else {
				Object[] data = new Object[this.customColumns.size() + 8];
				data[0] = event.name();
				data[1] = pos == ChunkPos.MARKER ? null : new ChunkPos(pos);
				data[2] = dim;
				data[3] = this.server.getOverworld().getTime();
				data[4] = Util.getMeasuringTimeNano();
				data[5] = thread.getName();
				data[6] = cause;
				data[7] = addition;
				ServerWorld world = this.server.getWorld(RegistryKey.of(Registry.WORLD_KEY, dim));
				int dataPos = 8;
				for(AccessingPath path : this.customColumns.values()) {
					try {
						data[dataPos] = path.access(world, ServerWorld.class);
					} catch (AccessingFailureException e) {
						data[dataPos] = e.getShortenedMsg();
					} finally {
						dataPos++;
					}
				}
				
				this.currentLog.println(data);
			}
		}
	}

	@Override
	protected String getLogFolderName() {
		return "chunklog";
	}

	public Set<ChunkEvent> listSubscribedEvents() {
		return Collections.unmodifiableSet(this.subscribedEvents);
	}

	public boolean isWorking() {
		return this.working;
	}

	public static boolean shouldSkip() {
		ChunkBehaviorLogger logger = MessMod.INSTANCE.getChunkLogger();
		return logger == null || !logger.isWorking();
	}

	public boolean addColumn(String name, AccessingPath path) {
		if(this.customColumns.containsKey(name)) {
			return false;
		} else {
			this.customColumns.put(name, path);
			return true;
		}
	}
	
	public boolean removeColumn(String name) {
		return this.customColumns.remove(name) != null;
	}
	
	public Set<String> getColumns() {
		return this.customColumns.keySet();
	}
}
