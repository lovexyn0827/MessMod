package lovexyn0827.mess.rendering.hud.data;

import java.lang.reflect.Field;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

public interface SidebarDataSender extends HudDataSender {
	void updateData();

	static SidebarDataSender create(MinecraftServer server) {
		if (MessMod.isDedicatedEnv()) {
			return new RemoteHudDataSender.Sidebar(server);
		} else {
			return new LocalSidebarDataStorage();
		}
	}

	default boolean addLine(Entity e, String fieldName, String name, AccessingPath path) {
		Field f = Reflection.getFieldFromNamed(e.getClass(), fieldName);
		return this.addCustomLine(new SidebarLine(new ListenedField(f, path, name), e));
	}
}
