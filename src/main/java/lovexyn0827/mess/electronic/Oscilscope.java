package lovexyn0827.mess.electronic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.ServerMicroTime;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

// TODO Configuration serialization
/**
 * Responsible for signal capture
 */
public final class Oscilscope {
	private static final byte DIGITAL_MODE = 0;
	private static final byte TRIG_MODE = 1;
	private static final byte TRIG_LEVEL = 2;
	private static final byte VISIBILITY = 3;
	private final Map<Pair<RegistryKey<World>, BlockPos>, Channel> channels = new HashMap<>();
	private final Int2ObjectMap<Channel> channelsById = new Int2ObjectOpenHashMap<>();
	private final OscilscopeDataSender dataSender;
	private final OscilscopeDataStorage dataStorage;
	private int nextChannelId = 0;
	private boolean digitalMode = false;
	
	public Oscilscope() {
		if (MessMod.isDedicatedEnv()) {
			if (MessMod.isDedicatedServerEnv()) {
				// Dedicated server
				this.dataSender = new RemoteOscilscopeDataSender();
				this.dataStorage = null;
			} else {
				// Dedicated client
				this.dataSender = null;
				this.dataStorage = new RemoteOscilscopeDataStorage();
			}
		} else {
			// Single-player
			LocalOscilscopeDataStorage localStroage = new LocalOscilscopeDataStorage();
			this.dataSender = localStroage;
			this.dataStorage = localStroage;
		}
	}
	
	public void update(World world, BlockPos pos, int level) {
		Pair<RegistryKey<World>, BlockPos> key = new Pair<>(world.getRegistryKey(), pos);
		if (this.channels.containsKey(key)) {
			this.channels.get(key).update(level);
		} else {
			int color = world.getBlockState(pos.down()).getTopMaterialColor(world, pos).color | 0xFF000000;
			Channel newChannel = new Channel(world.getRegistryKey(), pos, color);
			this.addChannel(key, newChannel);
			this.dataSender.sendNewChannel(newChannel);
			newChannel.update(level);
		}
	}

	public OscilscopeDataSender getDataSender() {
		return this.dataSender;
	}

	public OscilscopeDataStorage getDataStorage() {
		return this.dataStorage;
	}

	public void setDigitalMode(boolean digitalMode) {
		this.digitalMode = digitalMode;
		this.uploadConfig(DIGITAL_MODE, (buf) -> buf.writeBoolean(digitalMode));
	}
	
	public void sendAllChannelsTo(ServerPlayerEntity player) {
		this.dataSender.sendChannelsTo(this.channels.values(), player);
	}
	
	public boolean isDigitalMode() {
		return this.digitalMode;
	}
	
	private void addChannel(Pair<RegistryKey<World>, BlockPos> key, Channel ch) {
		this.channels.put(key, ch);
		this.channelsById.put(ch.id, ch);
	}
	
	@Environment(EnvType.CLIENT)
	Channel deserializeChannel(CompoundTag tag) {
		int id = tag.getInt("Id");
		RegistryKey<World> dim = RegistryKey.of(Registry.DIMENSION, 
				Identifier.tryParse(tag.getString("Dimension")));
		int[] posArr = tag.getIntArray("Pos");
		BlockPos pos = new BlockPos(posArr[0], posArr[1], posArr[2]);
		int color = tag.getInt("Color");
		TrigMode trigMode = TrigMode.values()[tag.getInt("TrigMode")];
		int trigLevel = tag.getInt("TrigLevel");
		Pair<RegistryKey<World>, BlockPos> key = new Pair<>(dim, pos);
		Channel newCh = new Channel(id, dim, pos, color, trigMode, trigLevel);
		this.addChannel(key, newCh);
		return newCh;
	}

	@Nullable
	Channel getChannel(int id) {
		return this.channelsById.get(id);
	}
	
	@Environment(EnvType.CLIENT)
	private void uploadConfig(byte type, Consumer<PacketByteBuf> writer) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(type);
		writer.accept(buf);
		CustomPayloadC2SPacket pkt = new CustomPayloadC2SPacket(Channels.OSCILSCOPE_CONF, buf);
		MessMod.INSTANCE.getClientNetworkHandler().send(pkt);
	}
	
	// Client to server
	public void handleConfigPacket(PacketByteBuf buf) {
		CustomPayloadS2CPacket broadcastPkt = new CustomPayloadS2CPacket(Channels.OSCILSCOPE_CONF_BROADCAST, 
				new PacketByteBuf(buf.copy()));
		this.applyConfigPacket(buf);
		MessMod.INSTANCE.getServerNetworkHandler().sendToEveryone(broadcastPkt);
	}

	@Environment(EnvType.CLIENT)
	public void handleConfigBroadcastPacket(PacketByteBuf buf) {
		this.applyConfigPacket(buf);
	}
	
	private void applyConfigPacket(PacketByteBuf buf) {
		switch (buf.readByte()) {
		case DIGITAL_MODE:
			this.digitalMode = buf.readBoolean();
			break;
		case TRIG_MODE:
			this.getChannel(buf.readInt()).trigMode = TrigMode.values()[buf.readInt()];
			break;
		case TRIG_LEVEL:
			this.getChannel(buf.readInt()).trigLevel = buf.readInt();
			break;
		case VISIBILITY:
			this.getChannel(buf.readInt()).visible = buf.readBoolean();
		}
	}

	@Environment(EnvType.CLIENT)
	public void handleDataPacket(PacketByteBuf buf) {
		if (this.dataStorage instanceof RemoteOscilscopeDataStorage) {
			((RemoteOscilscopeDataStorage) this.dataStorage).handlePacket(buf);
		}
	}
	
	final class Channel {
		private final int id;
		private final RegistryKey<World> dimension;
		private final BlockPos pos;
		private final int color;
		private TrigMode trigMode = TrigMode.NONE;
		private int trigLevel = 1;
		private int prevLevel = -1;
		private boolean visible = true;

		private Channel(RegistryKey<World> dimension, BlockPos pos, int color) {
			this.id = Oscilscope.this.nextChannelId++;
			this.dimension = dimension;
			this.pos = pos;
			this.color = color;
		}
		
		/**
		 * Only for deserialization
		 */
		private Channel(int id, RegistryKey<World> dimension, BlockPos pos, int color, TrigMode trigMode,
				int trigLevel) {
			this.id = id;
			this.dimension = dimension;
			this.pos = pos;
			this.color = color;
			this.trigMode = trigMode;
			this.trigLevel = trigLevel;
		}

		void update(int level) {
			if (level == prevLevel) {
				return;
			}
			
			Oscilscope.this.dataSender.sendEdge(this, level);
			boolean risingEdge;
			boolean fallingEdge;
			if (Oscilscope.this.digitalMode) {
				risingEdge = level > 0 && this.prevLevel <= 0;
				fallingEdge = level <= 0 && this.prevLevel > 0;
			} else {
				risingEdge = level >= this.trigLevel && this.prevLevel < this.trigLevel;
				fallingEdge = level < this.trigLevel && this.prevLevel >= this.trigLevel;
			}
			
			if (this.trigMode.shouldTrigger(risingEdge, fallingEdge)) {
				this.trigger(risingEdge);
			}
			
			this.prevLevel = level;
		}

		private void trigger(boolean risingEdge) {
			Trigger trig = new Trigger(this, ServerMicroTime.current(), risingEdge);
			Oscilscope.this.dataSender.sendTrigger(trig);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Objects.hash(dimension, pos);
			return result;
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
			Channel other = (Channel) obj;
			return Objects.equals(this.dimension, other.dimension) && Objects.equals(this.pos, other.pos);
		}
		
		@Override
		public String toString() {
			return "CH" + this.id;
		}
		
		int getColor() {
			return this.color;
		}

		@Environment(EnvType.CLIENT)
		void setTrigMode(TrigMode mode) {
			this.trigMode = mode;
			Oscilscope.this.uploadConfig(TRIG_MODE, (buf) -> {
				buf.writeInt(this.id);
				buf.writeInt(this.trigMode.ordinal());
			});
		}

		@Environment(EnvType.CLIENT)
		void setTrigLevel(int level) {
			this.trigLevel = level;
			Oscilscope.this.uploadConfig(TRIG_LEVEL, (buf) -> {
				buf.writeInt(this.id);
				buf.writeInt(this.trigLevel);
			});
		}

		public TrigMode getTrigMode() {
			return this.trigMode;
		}

		public int getTrigLevel() {
			return this.trigLevel;
		}

		public int getId() {
			return this.id;
		}

		public String getDimensionId() {
			return this.dimension.getValue().getPath();
		}
		
		public BlockPos getPos() {
			return this.pos;
		}

		@Environment(EnvType.CLIENT)
		public void setVisible(boolean visible) {
			this.visible = visible;
			if (OptionManager.hayOscilscopeChannelVisibilityBroadcast) {
				Oscilscope.this.uploadConfig(VISIBILITY, (buf) -> {
					buf.writeInt(this.id);
					buf.writeBoolean(visible);
				});
			}
		}
		
		public boolean isVisible() {
			return this.visible;
		}

		public CompoundTag toTag() {
			CompoundTag tag = new CompoundTag();
			tag.putInt("Id", this.id);
			tag.putString("Dimension", this.dimension.getValue().toString());
			tag.putIntArray("Pos", new int[] { this.pos.getX(), this.pos.getY(), this.pos.getZ()});
			tag.putInt("Color", this.color);
			tag.putInt("TrigMode", this.trigMode.ordinal());
			tag.putInt("TrigLevel", this.trigLevel);
			return tag;
		}
	}
	
	// Roughly 48 bytes
	static class Edge {
		final ServerMicroTime time;
		final int newLevel;
		
		public Edge(ServerMicroTime time, int newLevel) {
			this.time = time;
			this.newLevel = newLevel;
		}
	}
	
	static class Trigger {
		final Channel channel;
		final ServerMicroTime time;
		final boolean rising;
		
		public Trigger(Channel channel, ServerMicroTime time, boolean rising) {
			this.channel = channel;
			this.time = time;
			this.rising = rising;
		}
	}
	
	static enum TrigMode {
		RISING(true, false), 
		FALLING(false, true), 
		BOTH(true, true), 
		NONE(false, false);

		private final boolean trigOnRising;
		private final boolean trigOnFailing;
		
		private TrigMode(boolean trigOnRising, boolean trigOnFailing) {
			this.trigOnRising = trigOnRising;
			this.trigOnFailing = trigOnFailing;
		}
		
		boolean shouldTrigger(boolean risingEdge, boolean fallingEdge) {
			return risingEdge && this.trigOnRising || fallingEdge && this.trigOnFailing;
		}
	}
}
