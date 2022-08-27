package lovexyn0827.mess.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;

@Mixin(NetworkState.class)
public interface NetworkStateAccessor {
	@Accessor("packetHandlers")
	Map<NetworkSide, ? extends NetworkState.PacketHandler<?>> getHandlerMap();
}
