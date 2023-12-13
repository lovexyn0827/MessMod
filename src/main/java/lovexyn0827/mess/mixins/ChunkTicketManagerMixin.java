package lovexyn0827.mess.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.command.LazyLoadCommand;
import lovexyn0827.mess.fakes.ChunkTaskPrioritySystemInterface;
import lovexyn0827.mess.fakes.ChunkTicketManagerInterface;
import lovexyn0827.mess.log.chunk.ChunkBehaviorLogger;
import lovexyn0827.mess.log.chunk.ChunkEvent;
import lovexyn0827.mess.options.OptionManager;
import lovexyn0827.mess.util.blame.BlamingMode;
import lovexyn0827.mess.util.blame.StackTrace;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

@Mixin(ChunkTicketManager.class)
public class ChunkTicketManagerMixin implements ChunkTicketManagerInterface {
	@Unique
	private ServerWorld world;
	
	@Shadow @Final
	private ChunkTaskPrioritySystem levelUpdateListener;
	
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
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.TICKET_ADDITION, pos, this.getDimesionId(), 
					Thread.currentThread(), 
					OptionManager.blamingMode == BlamingMode.DISABLED ? null : StackTrace.current().blame(), 
					ticket);
		}
	}
	
	@Inject(method = "removeTicket(JLnet/minecraft/server/world/ChunkTicket;)V", 
			at = @At(value = "RETURN")
	)
	private void onTicketRemoved(long pos, ChunkTicket<?> ticket, CallbackInfo ci) {
		if(!ChunkBehaviorLogger.shouldSkip()) {
			MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.TICKET_REMOVAL, pos, this.getDimesionId(), 
					Thread.currentThread(), StackTrace.blameCurrent(), ticket);
		}
	}
	
	@Inject(method = "shouldTickEntities", at = @At("HEAD"), cancellable = true)
	private void tickEntityIfNeeded(long pos, CallbackInfoReturnable<Boolean> cir) {
		if(!LazyLoadCommand.LAZY_CHUNKS.isEmpty()) {
			if(LazyLoadCommand.LAZY_CHUNKS.containsKey(this.world.getRegistryKey())
					|| LazyLoadCommand.LAZY_CHUNKS.get(this.world.getRegistryKey()).contains(pos)) {
				cir.setReturnValue(false);
				cir.cancel();
			}
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	protected void onTickNoArg(CallbackInfoReturnable<Boolean> cir) {
		if(ChunkBehaviorLogger.shouldSkip()) {
			return;
		}
		
		MessMod.INSTANCE.getChunkLogger().onEvent(ChunkEvent.CTM_TICK, ChunkPos.MARKER, 
				this.getDimesionId(), Thread.currentThread(), StackTrace.blameCurrent(), 
				null);
	}

	@Override
	public Identifier getDimesionId() {
		return this.world.getRegistryKey().getValue();
	}

	@Override
	public void initWorld(ServerWorld world) {
		this.world = world;
		// This is necessary since we couldn't ensure that advanced CTPSMixin is applied.
		if(this.levelUpdateListener instanceof ChunkTaskPrioritySystemInterface) {
			((ChunkTaskPrioritySystemInterface) this.levelUpdateListener).initWorld(world);
		}
	}
}
