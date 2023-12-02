package lovexyn0827.mess.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.RemoteShapeCache;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.rendering.hud.data.HudDataStorage;
import lovexyn0827.mess.rendering.hud.data.RemoteHudDataStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public class MessClientNetworkHandler {
	private static final Map<Identifier, PacketHandler> PACKET_HANDLERS = Maps.newHashMap();
	private MinecraftClient client;

	public MessClientNetworkHandler(MinecraftClient mc) {
		this.client = mc;
	}

	public boolean handlePacket(CustomPayloadS2CPacket packet) {
		try {
			Identifier id = packet.getChannel();
			PacketHandler handler = PACKET_HANDLERS.get(id);
			if(handler != null) {
				handler.onPacket(packet);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public void send(CustomPayloadC2SPacket packet) {
		ClientPlayNetworkHandler handler = client.getNetworkHandler();
		if(handler != null) {
			handler.sendPacket(packet);
		}
	}

	public void sendVersion() {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(Channels.CHANNEL_VERSION);
		buf.writeString(FabricLoader.getInstance().getModContainer("messmod").get().getMetadata()
				.getVersion().getFriendlyString());
		CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(Channels.VERSION, buf);
		this.send(packet);
	}

	private static void register(Identifier hud, PacketHandler handler) {
		PACKET_HANDLERS.put(hud, handler);
	}
	
	static {
		register(Channels.HUD, (packet) -> {
			PacketByteBuf buffer = packet.getData();
			HudType type = buffer.readEnumConstant(HudType.class);
			NbtCompound tag = buffer.readNbt();
			HudDataStorage cache = MessMod.INSTANCE.getClientHudManager().getData(type);
			if (cache instanceof RemoteHudDataStorage) {
				((RemoteHudDataStorage) cache).pushData(tag);
			}
		});
		register(Channels.SHAPE, (packet) -> {
			((RemoteShapeCache) MessMod.INSTANCE.shapeCache).handlePacket(packet);
		});
		register(Channels.OPTIONS, (packet) -> {
			try {
				OptionManager.loadFromServer(new ByteArrayInputStream(packet.getData().readString().getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public interface PacketHandler {
		void onPacket(CustomPayloadS2CPacket packet);
	}
}
