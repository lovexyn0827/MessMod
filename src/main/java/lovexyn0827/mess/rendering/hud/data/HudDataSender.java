package lovexyn0827.mess.rendering.hud.data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import lovexyn0827.mess.MessMod;
import lovexyn0827.mess.rendering.hud.HudType;
import lovexyn0827.mess.util.ListenedField;
import lovexyn0827.mess.util.Reflection;
import lovexyn0827.mess.util.WrappedPath;
import lovexyn0827.mess.util.access.AccessingPath;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * @author lovexyn0827
 * @date 2022/7/14
 */
public interface HudDataSender {
	void updateData(Entity entity);
	Map<String, HudLine> getLines();

	/**
	 * Returns a list of lines added by the user.
	 * Modifying the collection doesn't update underlying list for HUD lines. 
	 * Use {@code getLines()} instead to add / remove lines from the HUD.
	 */
	default Map<String, HudLine> getCustomLines() {
		return this.getLines().entrySet().stream()
				.filter((e) -> !(e.getValue() instanceof BuiltinHudInfo))
				.collect(TreeMap::new, (map, e) -> map.put(e.getKey(), e.getValue()), (map1, map2) -> map1.putAll(map2));
	}
	
	default boolean hasDuplication(HudLine line) {
		Map<String, HudLine> customLines = this.getCustomLines();
		return customLines.containsKey(line.getName()) || customLines.containsValue(line);
	}
	
	/**
	 * @implNote Custom lines whose name is the same as the one of the names of in built-in lines and one of the other
	 *  custom lines should be rejected.
	 */
	default boolean addCustomLine(HudLine line) {
		if(this.hasDuplication(line)) {
			return false;
		} else {
			this.getLines().put(line.getName(), line);
			return true;
		}
	}
	
	/**
	 * @implNote Custom lines whose name is the same as the one of the names of in built-in lines should be rejected.
	 */
	default boolean addOrReplaceCustomLine(HudLine line) {
		this.getLines().put(line.getName(), line);
		return true;
	}
	
	default boolean removeCustomLine(String name) {
		return this.getLines().remove(name) != null;
	}
	
	default boolean addField(Class<?> cl, String field) {
		return this.addField(cl, field, field, AccessingPath.DUMMY);
	}
	
	default boolean addOrReplaceField(Class<?> cl, String field) {
		return this.addOrReplaceField(cl, field, field, AccessingPath.DUMMY);
	}
	
	static HudLine createFieldLine(Class<?> cl, String field, String name, AccessingPath path) {
		if ("-THIS-".equals(field)) {
			return new WrappedPath(path, name);
		}
		
		Field f = Reflection.getFieldFromNamed(cl, field);
		return new ListenedField(f, path, name);
	}
	
	default boolean addField(Class<?> cl, String field, String name, AccessingPath path) {
		return this.addCustomLine(createFieldLine(cl, field, name, path));
	}
	
	default boolean addOrReplaceField(Class<?> cl, String field, String name, AccessingPath path) {
		return this.addOrReplaceCustomLine(createFieldLine(cl, field, name, path));
	}

	default List<HudLine> getListenedFields() {
		return this.getCustomLines().values().stream()
				.collect(Collectors.toList());
	}
	
	/**
	 * SIDEBAR is not allowed here!
	 */
	public static HudDataSender createHudDataSenderer(HudType type, MinecraftServer server) {
		if (type == HudType.SIDEBAR) {
			throw new IllegalArgumentException("Data senderer of sidebars cannot be created here!");
		}
		
		if(MessMod.isDedicatedEnv()) {
			return new RemoteHudDataSender(server, type, true);
		} else {
			switch(type) {
			case TARGET :  
			case SERVER_PLAYER : 
			case CLIENT_PLAYER : 
				return new LocalDefaultHudDataStorage();
			default:
				throw new IllegalArgumentException();
			}
		}
	}
}
