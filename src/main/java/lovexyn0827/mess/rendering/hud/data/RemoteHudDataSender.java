package lovexyn0827.mess.rendering.hud.data;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.util.TickingPhase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class RemoteHudDataSender implements HudDataSender {
	/** Used to determine the delta */
	protected NbtCompound lastData = new NbtCompound();
	protected final List<HudLine> customLines = Lists.newArrayList();
	protected final MinecraftServer server;
	private final HudType type;

	public RemoteHudDataSender(MinecraftServer server, HudType type) {
		this.server = server;
		this.type = type;
	}
	
	public void updateData(Entity entity) {
		NbtCompound data = new NbtCompound();
		List<String> unused = Lists.newArrayList(lastData.getKeys());
		Stream<HudLine> lines = this.streamAllLines();
		if (entity != null) {
			lines.forEach((l) -> {
				if (this.tryPutData(entity, l, data)) {
					unused.remove(l.getName());
				}
			});
		}
		
		NbtList toRemove = new NbtList();
		unused.forEach((n) -> {
			toRemove.add(NbtString.of(n));
		});
		data.put("ToRemove", toRemove);
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(type);
		buffer.writeNbt(data);
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Channels.HUD, buffer);
		this.server.getPlayerManager().getPlayerList().stream()
				.filter((p) -> ((HudDataSubscribeState) p.networkHandler).isSubscribed(this.type))
				.forEach((p) -> p.networkHandler.sendPacket(packet));
	}
	
	protected Stream<HudLine> streamAllLines() {
		return Stream.concat(Stream.of(BuiltinHudInfo.values()), this.customLines.stream());
	}

	private boolean tryPutData(Entity entity, HudLine l, NbtCompound data) {
		String name = l.getName();
		if(l.canGetFrom(entity)) {
			NbtElement last = this.lastData.get(name);
			String value = l.getFrom(entity);
			if(last == null || !last.asString().equals(value.toString())) {
				data.putString(name, value);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public List<HudLine> getCustomLines() {
		return this.customLines;
	}
	
	public static class Player extends RemoteHudDataSender implements PlayerHudDataSender {
		public Player(MinecraftServer server, HudType type) {
			super(server, type);
		}

		@Override
		public void updatePlayer() {
		}

		@Override
		public void updateData() {
		}
		
	}
	
	public static class Sidebar extends RemoteHudDataSender implements SidebarDataSender {
		public Sidebar(MinecraftServer server) {
			super(server, HudType.SIDEBAR);
			this.registerTickingEvents();
		}

		public void updateData(TickingPhase phase, @Nullable ServerWorld world) {
			NbtCompound data = new NbtCompound();
			List<String> unused = Lists.newArrayList(this.lastData.getKeys());
			Stream<HudLine> lines = this.streamAllLines();
			lines.forEach((l) -> {
				if(l instanceof SidebarLine) {
					SidebarLine line = (SidebarLine) l;
					if(SidebarDataSender.shouldUpdate(line, phase, world)) {
						Object ob = line.get();
						data.put(line.getName(), NbtString.of(ob.toString()));
					}
				} else {
					throw new IllegalStateException("Only SidebarLines are permitted");
				}
				
				unused.remove(l.getName());
			});
			NbtList toRemove = new NbtList();
			unused.forEach((n) -> {
				toRemove.add(NbtString.of(n));
			});
			data.put("ToRemove", toRemove);
			PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
			buffer.writeEnumConstant(HudType.SIDEBAR);
			buffer.writeNbt(data);
			CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Channels.HUD, buffer);
			this.server.getPlayerManager().getPlayerList().stream()
					//.filter((p) -> ((HudDataSubscribeState) p.networkHandler).isSubscribed(HudType.SIDEBAR))
					.forEach((p) -> p.networkHandler.sendPacket(packet));
		}
		
		@Override
		protected Stream<HudLine> streamAllLines() {
			return this.customLines.stream();
		}
	}
}
