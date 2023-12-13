package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.MessClientNetworkHandler;
import lovexyn0827.mess.network.MessModPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
	@Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
	private static void tryReadMessModPacket(Identifier id, PacketByteBuf buf, 
			CallbackInfoReturnable<CustomPayload> cir) {
		MessClientNetworkHandler handler = MessMod.INSTANCE.getClientNetworkHandler();
		if (handler != null && handler.isValidPackedId(id)) {
			cir.setReturnValue(new MessModPayload(id, buf));
			cir.cancel();
		}
	}
	
}
