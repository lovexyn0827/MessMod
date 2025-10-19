package lovexyn0827.mess.util;

import java.util.Objects;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import net.minecraft.network.PacketByteBuf;

public final class ServerMicroTime implements Comparable<ServerMicroTime> {
	public static final ServerMicroTime PRE_HISTORY = new ServerMicroTime(-1, ServerTickingPhase.WEATHER_CYCLE);
	public static final ServerMicroTime END_OF_TIME = new ServerMicroTime(Long.MAX_VALUE, 
			ServerTickingPhase.REST);
	public final long gameTime;
	public final ServerTickingPhase phase;
	
	public ServerMicroTime(long gameTime, ServerTickingPhase phase) {
		this.gameTime = gameTime;
		this.phase = phase;
	}
	
	public static ServerMicroTime current() {
		return new ServerMicroTime(MessMod.INSTANCE.getGameTime(), ServerTickingPhase.current());
	}

	@Override
	public int compareTo(ServerMicroTime o) {
		if (this.gameTime - o.gameTime != 0) {
			return (int) (this.gameTime - o.gameTime);
		}
		
		return this.phase.ordinal() - o.phase.ordinal();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.gameTime, this.phase);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (!(o instanceof ServerMicroTime)) {
			return false;
		}
		
		ServerMicroTime other = (ServerMicroTime) o;
		return other.gameTime == this.gameTime && other.phase.ordinal() == this.phase.ordinal();
	}
	
	@Override
	public String toString() {
		return String.format("%d @ %s", this.gameTime, this.phase.name());
	}
	
	public void write(PacketByteBuf buf) {
		buf.writeLong(this.gameTime);
		buf.writeByte(this.phase.ordinal());
	}
	
	public static ServerMicroTime read(PacketByteBuf buf) {
		return new ServerMicroTime(buf.readLong(), ServerTickingPhase.values()[buf.readByte()]);
	}
}
