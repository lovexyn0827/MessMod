package lovexyn0827.mess.network;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.fakes.HudDataSubscribeState;
import lovexyn0827.mess.mixins.CustomPayloadC2SPacketAccessor;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.rendering.hud.HudType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MessServerNetworkHandler {
	@SuppressWarnings("unused")
	private final MinecraftServer server;

	public MessServerNetworkHandler(MinecraftServer server) {
		this.server = server;
	}
	
	public boolean handlePacket(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
		try {
			CustomPayloadC2SPacketAccessor accessor = (CustomPayloadC2SPacketAccessor) packet;
			Identifier channel = accessor.getMessChannel();
			PacketByteBuf buf = accessor.getMessData();
			if(channel.equals(Channels.HUD)) {
				HudType type = buf.readEnumConstant(HudType.class);
				if (buf.readBoolean()) {
					((HudDataSubscribeState) player.networkHandler).subscribe(type);
				} else {
					((HudDataSubscribeState) player.networkHandler).subscribe(type);
				}
				
				return true;
			} else if (channel.equals(Channels.VERSION)) {
				int protocol = buf.readInt();
				String ver = buf.readString(32767);
				MessMod.LOGGER.info("Player {} joined the game with MessMod {} (Protocol Version: {})", 
						player.getName().asString(), ver, protocol);
				if(protocol != Channels.CHANNEL_VERSION) {
					MessMod.LOGGER.warn("But note that the protocol version of the client differs from the one here.");
				}
				
				return true;
			} else if (channel.equals(Channels.UNDO)) {
				if(OptionManager.blockPlacementHistory) {
					MessMod.INSTANCE.getPlacementHistory().undo(player);
				}
			} else if (channel.equals(Channels.REDO)) {
				if(OptionManager.blockPlacementHistory) {
					MessMod.INSTANCE.getPlacementHistory().redo(player);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
