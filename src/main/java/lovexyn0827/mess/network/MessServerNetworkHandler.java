package lovexyn0827.mess.network;

import java.util.Map;

import com.google.common.collect.Maps;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.fakes.ServerPlayerEntityInterface;
import lovexyn0827.mess.mixins.CustomPayloadC2SPacketAccessor;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.util.FormattedText;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MessServerNetworkHandler {
	private static final Map<Identifier, PacketHandler> PACKET_HANDLERS = Maps.newHashMap();
	private final MinecraftServer server;

	public MessServerNetworkHandler(MinecraftServer server) {
		this.server = server;
	}
	
	public boolean handlePacket(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
		try {
			CustomPayloadC2SPacketAccessor accessor = (CustomPayloadC2SPacketAccessor) packet;
			Identifier channel = accessor.getMessChannel();
			PacketByteBuf buf = accessor.getMessData();
			PacketHandler handler = PACKET_HANDLERS.get(channel);
			if(handler != null) {
				handler.onPacket(player, channel, buf);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private static void register(Identifier hud, PacketHandler handler) {
		PACKET_HANDLERS.put(hud, handler);
	}
	
	static {
		register(Channels.HUD, (player, channel, buf) -> {
			HudType type = buf.readEnumConstant(HudType.class);
			if (buf.readBoolean()) {
				((HudDataSubscribeState) player.networkHandler).subscribe(type);
			} else {
				((HudDataSubscribeState) player.networkHandler).subscribe(type);
			}
		});
		register(Channels.VERSION, (player, channel, buf) -> {
			int protocol = buf.readInt();
			String ver = buf.readString(32767);
			MessMod.LOGGER.info("Player {} joined the game with MessMod {} (Protocol Version: {})", 
					player.getName().getContent(), ver, protocol);
			if(protocol != Channels.CHANNEL_VERSION) {
				MessMod.LOGGER.warn("But note that the protocol version of the client differs from the one here.");
				player.sendMessage(new FormattedText("misc.protver.err", "c").asMutableText(), false);
			}
		});
		register(Channels.UNDO, (player, channel, buf) -> {
			if(OptionManager.blockPlacementHistory) {
				((ServerPlayerEntityInterface) player).getBlockPlacementHistory().undo();
			}
		});
		register(Channels.REDO, (player, channel, buf) -> {
			if(OptionManager.blockPlacementHistory) {
				((ServerPlayerEntityInterface) player).getBlockPlacementHistory().redo();
			}
		});
	}
	
	public interface PacketHandler {
		void onPacket(ServerPlayerEntity player, Identifier channel, PacketByteBuf buf);
	}

	public void sendToEveryone(CustomPayloadS2CPacket packet) {
		this.server.getPlayerManager().sendToAll(packet);
	}
}
