package lovexyn0827.mess.rendering;

import java.util.HashMap;
import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

public class RemoteShapeCache extends ShapeCache {
	private final ThreadExecutor<RemoteShapeTask> shapeHandler = new ThreadExecutor<RemoteShapeTask>("MessMod Shape Handler") {
		private final Thread thread = createThread();
		private boolean running = true;
		
		@Override
		protected RemoteShapeTask createTask(Runnable runnable) {
			return new RemoteShapeTask(runnable);
		}

		private Thread createThread() {
			Thread t = new Thread(() -> {
				while(this.running) {
					this.runTasks();
					this.waitForTasks();
				}
			}, "MessMod Shape Handler");
			t.setUncaughtExceptionHandler((th, e) -> {
				MessMod.LOGGER.warn("Failed to handle shapes: {}", e);
			});
			t.start();
			return t;
		}

		@Override
		protected boolean canExecute(RemoteShapeTask task) {
			return true;
		}

		@Override
		protected Thread getThread() {
			return this.thread;
		}
		
		@Override
		public void close() {
			super.close();
			this.cancelTasks();
			this.running = false;
		}
	};
	
	RemoteShapeCache() {
		backend.put(World.OVERWORLD, new HashMap<>());
		backend.put(World.NETHER, new HashMap<>());
		backend.put(World.END, new HashMap<>());
	}

	public synchronized void handlePacket(CustomPayloadS2CPacket packet) {
		this.shapeHandler.execute(() -> {
			synchronized (this) {
				PacketByteBuf buffer = packet.getData();
				switch(buffer.readEnumConstant(RemoteShapeSender.UpdateMode.class)) {
				case ADD_SHAPE : 
					// Mode - Dimension - Space - Tag
					RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, buffer.readIdentifier());
					ShapeSpace space = new ShapeSpace(buffer.readString());
					NbtCompound tag = buffer.readNbt();
					this.getShapesInDimension(dim).computeIfAbsent(space, (s) -> Sets.newHashSet())
							.add(Shape.fromTag(tag));
					break;
				case CLEAR_SPACE : 
					ShapeSpace space2 = new ShapeSpace(buffer.readString());
					this.clearSpace(space2);
					break;
				case TICK : 
					this.time = buffer.readLong();
				}
			}
		});
	}
	
	@Override
	public void close() {
		this.shapeHandler.close();
	}
	
	protected static class RemoteShapeTask implements Runnable {
		private final Runnable operation;

		public RemoteShapeTask(Runnable runnable) {
			this.operation = runnable;
		}

		@Override
		public void run() {
			this.operation.run();
		}
	}
}
