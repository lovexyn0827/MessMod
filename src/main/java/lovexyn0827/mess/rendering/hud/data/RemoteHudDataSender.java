package lovexyn0827.mess.rendering.hud.data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.network.Channels;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class RemoteHudDataSender implements HudDataSender {
	/** Used to determine the delta */
	protected NbtCompound lastData = new NbtCompound();
	protected final List<HudLine> lines = Lists.newArrayList();
	protected final MinecraftServer server;
	private final HudType type;

	public RemoteHudDataSender(MinecraftServer server, HudType type, boolean addDefaultLines) {
		this.server = server;
		this.type = type;
		if (addDefaultLines) {
			for(HudLine l : BuiltinHudInfo.values()) {
				this.lines.add(l);
			}
		}
	}

	@Override
	public Collection<HudLine> getLines() {
		return this.lines;
	}
	
	public void updateData(Entity entity) {
		NbtCompound data = new NbtCompound();
		List<String> unused = Lists.newArrayList(this.lastData.getKeys());
		Stream<HudLine> lines = this.streamAllLines();
		if (entity != null) {
			lines.forEach((l) -> {
				if (this.tryPutData(entity, l, data)) {
					unused.remove(l.getName());
				}
			});
		}
		

		this.lastData = this.lastData.copyFrom(data);
		NbtList toRemove = new NbtList();
		unused.forEach((n) -> {
			toRemove.add(NbtString.of(n));
			this.lastData.remove(n);
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
		return this.lines.stream();
	}

	/**
	 * @return true if the line is applicable for the given entity, false otherwise.
	 */
	private boolean tryPutData(Entity entity, HudLine l, NbtCompound data) {
		String name = l.getName();
		if(l.canGetFrom(entity)) {
			NbtElement last = this.lastData.get(name);
			String value = l.getFrom(entity);
			if(last == null || !last.asString().equals(value)) {
				data.putString(name, value);
			}
			
			return true;
		}
		
		return false;
	}
	
	public static class Sidebar extends RemoteHudDataSender implements SidebarDataSender {
		public Sidebar(MinecraftServer server) {
			super(server, HudType.SIDEBAR, false);
			this.registerTickingEvents();
		}

		@Override
		public void updateData(TickingPhase phase, @Nullable World world) {
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
			this.lastData = this.lastData.copyFrom(data);
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
					//TODO .filter((p) -> ((HudDataSubscribeState) p.networkHandler).isSubscribed(HudType.SIDEBAR))
					.forEach((p) -> p.networkHandler.sendPacket(packet));
		}
		
		@Override
		protected Stream<HudLine> streamAllLines() {
			return this.lines.stream();
		}
	}
}
