package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.Packet;

@Mixin(NetworkState.PacketHandler.class)
public interface NetworkStatePacketHandlerAccessor {
	@Invoker("getPacketIdToPacketMap")
	Int2ObjectMap<Class<? extends Packet<?>>> getPacketTypeMapForMess();
}
