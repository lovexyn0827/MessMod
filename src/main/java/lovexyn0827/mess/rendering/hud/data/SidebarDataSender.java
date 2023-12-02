package lovexyn0827.mess.rendering.hud.data;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.TranslatableException;
import lovexyn0827.mess.util.WrappedPath;
import lovexyn0827.mess.util.access.AccessingPath;
import lovexyn0827.mess.util.phase.ClientTickingPhase;
import lovexyn0827.mess.util.phase.ServerTickingPhase;
import lovexyn0827.mess.util.phase.TickingPhase;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface SidebarDataSender extends HudDataSender {
	void updateData(TickingPhase phase, @Nullable World world);
	
	default void registerTickingEvents() {
		ServerTickingPhase.addEventToAll(this::updateData);
		ClientTickingPhase.addEventToAll(this::updateData);
	}
	
	static boolean shouldUpdate(SidebarLine line, TickingPhase phase, @Nullable World world) {
		World entityWorld = line.entity.world;
		return line.updatePhase == phase && line.canGet() && 
				(entityWorld == world || phase.isNotInAnyWorld() ||
				entityWorld instanceof ServerWorld && phase instanceof ClientTickingPhase ||
				!(entityWorld instanceof ServerWorld) && phase instanceof ServerTickingPhase);
	}

	static SidebarDataSender create(MinecraftServer server) {
		if (MessMod.isDedicatedEnv()) {
			return new RemoteHudDataSender.Sidebar(server);
		} else {
			return new LocalSidebarDataStorage();
		}
	}

	default boolean addLine(Entity e, String fieldName, String name, TickingPhase phase, AccessingPath path) {
		if ("-THIS-".equals(fieldName)) {
			return this.addCustomLine(new SidebarLine(new WrappedPath(path, name), e, phase));
		}
		
		Field f = Reflection.getFieldFromNamed(e.getClass(), fieldName);
		if (f == null) {
			throw new TranslatableException("exp.nofield", fieldName, e.getClass().getSimpleName());
		}
		
		return this.addCustomLine(new SidebarLine(new ListenedField(f, path, name), e, phase));
	}
}
