package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import lovexyn0827.mess.options.OptionManager;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;

@Mixin(ChunkTicketManager.class)
public class ChunkTicketManagerMixin {
	@Inject(method = "addTicket(JLnet/minecraft/server/world/ChunkTicket;)V", 
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void returnIfNeeded(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
		if(OptionManager.rejectChunkTicket.contains(ticket.getType())) {
			ci.cancel();
		}
	}

}
