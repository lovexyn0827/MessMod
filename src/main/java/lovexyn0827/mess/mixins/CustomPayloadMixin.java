package lovexyn0827.mess.mixins;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import lovexyn0827.mess.network.MessModPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Type;

@Mixin(CustomPayload.class)
public interface CustomPayloadMixin {
	@ModifyVariable(method = "createCodec", at = @At("HEAD"), index = 1)
	private static List<Type<?, ?>> appendMessModPacketCodec(List<Type<?, ?>> types) {
		ArrayList<Type<?, ?>> newTypes = new ArrayList<>(types);
		newTypes.add(new Type<>(MessModPayload.ID, MessModPayload.CODEC));
		return newTypes;
	}
}
