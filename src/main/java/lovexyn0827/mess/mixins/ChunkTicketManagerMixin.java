package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.blame.BlamingMode;
import lovexyn0827.mess.util.blame.StackTrace;
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

	@Inject(method = "addTicket(JLnet/minecraft/server/world/ChunkTicket;)V", 
			at = @At(value = "RETURN")
	)
	private void onTicketAdded(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
		if(!ChunkBehaviorLogger.shouldSkip()) {
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.TICKET_ADDITION, pos, null, Thread.currentThread(), 
					OptionManager.blamingMode == BlamingMode.DISABLED ? null : StackTrace.current().blame(), 
					ticket);
		}
	}
	
	@Inject(method = "removeTicket(JLnet/minecraft/server/world/ChunkTicket;)V", 
			at = @At(value = "RETURN")
	)
	private void onTicketRemoved(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
		if(!ChunkBehaviorLogger.shouldSkip()) {
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.TICKET_REMOVAL, pos, null, Thread.currentThread(), 
					StackTrace.blameCurrent(), ticket);
		}
	}
}
