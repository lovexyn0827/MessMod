package lovexyn0827.mess.rendering.hud.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class LocalDataStorage implements HudDataSender, HudDataStorage {
	private Map<HudLine, Object> data = new TreeMap<>(HudLine::compare);
	private Map<String, HudLine> lines = new TreeMap<>();
	
	public LocalDataStorage() {
		for(HudLine l : BuiltinHudInfo.values()) {
			this.lines.put(l.getName(), l);
		}
	}

	@Override
	public Map<String, HudLine> getLines() {
		return this.lines;
	}

	@Override
	public synchronized void updateData(Entity entity) {
		this.data.clear();
		if (entity == null) return;
		this.lines.forEach((name, f) -> {
			if(f.canGetFrom(entity)) {
				this.data.put(f, f.getFrom(entity));
			}
		});
	}

	@Override
	public synchronized void forEach(BiConsumer<String, Object> action) {
		this.data.forEach((l, d) -> {
			action.accept(l.getName(), d);
		});
	}

	@Override
	@Deprecated
	public Iterator<Entry<HudLine, Object>> iterator() {
		return this.data.entrySet().iterator();
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public synchronized Object get(HudLine id) {
		return this.data.get(id);
	}
}
