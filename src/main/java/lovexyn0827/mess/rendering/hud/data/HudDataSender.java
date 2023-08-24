package lovexyn0827.mess.rendering.hud.data;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.rendering.hud.LookingAtEntityHud;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.WrappedPath;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author lovexyn0827
 * @date 2022/7/14
 */
public interface HudDataSender {
	void updateData(Entity entity);
	Collection<HudLine> getCustomLines();
	
	/**
	 * @implNote Custom lines whose name is the same as the one of the names of in built-in lines and one of the other
	 *  custom lines should be rejected.
	 */
	default boolean addCustomLine(HudLine line) {
		boolean hasDuplication = this.getCustomLines().stream().anyMatch((l0) -> {
			return l0.getName().equals(line.getName()) || l0.equals(line);
		});
		if(hasDuplication || BuiltinHudInfo.BY_TITLE.containsKey(line.getName())) {
			return false;
		} else {
			this.getCustomLines().add(line);
			return true;
		}
	}
	
	default boolean removeCustomLine(String name) {
		return this.getCustomLines().removeIf(((line) -> line.getName().equals(name)));
	}
	
	default boolean addField(Class<?> cl, String field) {
		return this.addField(cl, field, field, AccessingPath.DUMMY);
	}
	
	default boolean addField(Class<?> cl, String field, String name, AccessingPath path) {
		if ("-THIS-".equals(field)) {
			return this.addCustomLine(new WrappedPath(path, name));
		}
		
		Field f = Reflection.getFieldFromNamed(cl, field);
		ListenedField lf = new ListenedField(f, path, name);
		return this.addCustomLine(lf);
	}

	default List<ListenedField> getListenedFields() {
		return this.getCustomLines().stream()
				.filter(ListenedField.class::isInstance)
				.map((l) -> (ListenedField) l)
				.collect(Collectors.toList());
	}
	
	public static HudDataSender createHudDataSenderer(HudType type, MinecraftServer server) {
		if(MessMod.isDedicatedEnv()) {
			return type.isPlayer() ? new RemoteHudDataSender.Player(server, type) : new RemoteHudDataSender(server, type);
		} else {
			switch(type) {
			case TARGET :  
				return new LocalDataStorage();
			case SERVER_PLAYER : 
				return new LocalPlayerDataStorage(true);
			case CLIENT_PLAYER : 
				return new LocalPlayerDataStorage(false);
			default:
				throw new IllegalArgumentException();
			}
		}
	}
	
	default void updateLookingAtEntityData(ServerPlayerEntity player) {
		this.updateData(LookingAtEntityHud.getTarget(player));
	}

}
