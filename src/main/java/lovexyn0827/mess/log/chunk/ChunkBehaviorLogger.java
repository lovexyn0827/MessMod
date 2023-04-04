package lovexyn0827.mess.log.chunk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.log.AbstractAchivingLogger;
import lovexyn0827.mess.log.CsvWriter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

public class ChunkBehaviorLogger extends AbstractAchivingLogger {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	public static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private CsvWriter currentLog;
	private final HashSet<ChunkEvent> subscribedEvents = new HashSet<>();
	private boolean working = false;

	public ChunkBehaviorLogger(MinecraftServer server) {
		super(server);
	}
	
	public void start() throws IOException {
		File logFile = this.getLogPath().resolve(DATE_FORMAT.format(new Date()) + ".csv").toFile();
		FileWriter writer = new FileWriter(logFile);
		this.currentLog = new CsvWriter.Builder()
				.addColumn("Event")
				.addColumn("Pos")
				.addColumn("Dimension")
				.addColumn("GameTime")
				.addColumn("RealTime")
				.addColumn("Thread")
				.addColumn("Cause")	// TODO
				.addColumn("Addition")
				.build(writer);
		this.working = true;
		this.hasCreatedAnyLog = true;
	}
	
	public void stop() throws IOException {
		this.working = false;
		this.currentLog.close();
		this.currentLog = null;
	}
	
	public void setSubscribed(ChunkEvent event, boolean enabled) {
		Objects.requireNonNull(event);
		if(enabled) {
			this.subscribedEvents.add(event);
		} else {
			this.subscribedEvents.remove(event);
		}
	}
	
	public synchronized void onEvent(ChunkEvent event, long pos, Identifier dim, Thread thread, 
			Object cause, String addition) {
		if(this.working && this.subscribedEvents.contains(event)) {
			this.currentLog.println(event.name(), 
					new ChunkPos(pos), 
					dim, 
					this.server.getOverworld().getTime(), 
					Util.getMeasuringTimeNano(), 
					thread.getName(), 
					cause, 
					addition);
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
}
