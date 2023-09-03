package lovexyn0827.mess.rendering;

import io.netty.buffer.Unpooled;
import lovexyn0827.mess.network.Channels;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class RemoteShapeSender implements ShapeSender {
	private final MinecraftServer server;
	

	public RemoteShapeSender(MinecraftServer server) {
		this.server = server;
	}

	@Override
	// Mode - Dimension - Space - Tag
	public void addShape(Shape shape, RegistryKey<World> dim, ShapeSpace space, ServerPlayerEntity player) {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(UpdateMode.ADD_SHAPE);
		buffer.writeIdentifier(dim.getValue());
		buffer.writeString(space.name);
		NbtCompound tag = new NbtCompound();
		buffer.writeNbt(shape.toTag(tag));
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Channels.SHAPE, buffer);
		if(player == null) {
			this.server.getPlayerManager().sendToDimension(packet, dim);
		} else {
			if(player.networkHandler != null) {
				player.networkHandler.sendPacket(packet);
			}
		}
	}

	@Override
	public void clearSpaceFromServer(ShapeSpace space, ServerPlayerEntity player) {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(UpdateMode.CLEAR_SPACE);
		buffer.writeString(space.name);
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Channels.SHAPE, buffer);
		this.server.getPlayerManager().sendToAll(packet);
	}

	@Override
	public void updateClientTime(long gt) {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeEnumConstant(UpdateMode.TICK);
		buffer.writeLong(gt);
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(Channels.SHAPE, buffer);
		this.server.getPlayerManager().sendToAll(packet);
	}
	
	public static enum UpdateMode {
		ADD_SHAPE, CLEAR_SPACE, TICK;
	}
}
