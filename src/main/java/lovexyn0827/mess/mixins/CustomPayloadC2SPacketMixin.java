package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.network.MessModPayload;
import lovexyn0827.mess.network.MessServerNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
	@Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
	private static void tryReadMessModPacket(Identifier id, PacketByteBuf buf, 
			CallbackInfoReturnable<CustomPayload> cir) {
		MessServerNetworkHandler handler = MessMod.INSTANCE.getServerNetworkHandler();
		if (handler != null && handler.isValidPackedId(id)) {
			cir.setReturnValue(new MessModPayload(id, buf));
			cir.cancel();
		}
	}
	
}
