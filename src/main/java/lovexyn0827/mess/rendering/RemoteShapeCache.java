package lovexyn0827.mess.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import lovexyn0827.mess.MessMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

public class RemoteShapeCache extends ShapeCache {
	private static final ThreadExecutor<Runnable> SHAPE_HANDLER = new ThreadExecutor<Runnable>("MessMod Shape Handler") {
		private final Thread thread = createThread();
		
		@Override
		protected Runnable createTask(Runnable runnable) {
			return runnable;
		}

		private Thread createThread() {
			Thread t = new Thread(() -> {
				while(true) {
					this.runTasks();
					this.waitForTasks();
				}
			}, "MessMod Shape Handler");
			t.setDaemon(true);
			t.start();
			t.setUncaughtExceptionHandler((th, e) -> {
				MessMod.LOGGER.warn("Failed to handle shapes: {}", e);
			});
			return t;
		}

		@Override
		protected boolean canExecute(Runnable task) {
			return true;
		}

		@Override
		protected Thread getThread() {
			return this.thread;
		}
		
	};
	
	RemoteShapeCache() {
		backend.put(World.OVERWORLD, new HashMap<>());
		backend.put(World.NETHER, new HashMap<>());
		backend.put(World.END, new HashMap<>());
	}

	@Override
	public synchronized Map<RegistryKey<World>, Map<ShapeSpace, Set<Shape>>> getAllShapes() {
		return backend;
	}

	public synchronized void handlePacket(CustomPayloadS2CPacket packet) {
		SHAPE_HANDLER.execute(() -> {
			synchronized (this) {
				PacketByteBuf buffer = packet.getData();
				switch(buffer.readEnumConstant(RemoteShapeSender.UpdateMode.class)) {
				case ADD_SHAPE : 
					// Mode - Dimension - Space - Tag
					RegistryKey<World> dim = RegistryKey.of(Registry.WORLD_KEY, buffer.readIdentifier());
					ShapeSpace space = new ShapeSpace(buffer.readString());
					NbtCompound tag = buffer.readNbt();
					this.getShapesInDimension(dim).computeIfAbsent(space, (s) -> Sets.newHashSet()).add(Shape.fromTag(tag));
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
	public long getTime() {
		return this.time;
	}
}
