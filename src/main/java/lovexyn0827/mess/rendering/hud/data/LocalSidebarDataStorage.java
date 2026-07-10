package lovexyn0827.mess.rendering.hud.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import lovexyn0827.mess.util.phase.TickingPhase;

import java.util.TreeMap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class LocalSidebarDataStorage implements SidebarDataSender, HudDataStorage {
	private TreeMap<HudLine, Object> data = new TreeMap<>(HudLine::compare);
	private Map<String, HudLine> lines = new TreeMap<>();
	
	public LocalSidebarDataStorage() {
		this.registerTickingEvents();
	}

	@Override
	public Map<String, HudLine> getLines() {
		return this.lines;
	}
	
	@Override
	public void updateData(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public Object get(HudLine id) {
		return this.data.get(id);
	}

	@Override
	public Iterator<Entry<HudLine, Object>> iterator() {
		return this.data.entrySet().iterator();
	}

	@Override
	public synchronized void updateData(TickingPhase phase, @Nullable World world) {
		this.data.keySet().removeIf((l) -> !this.lines.containsValue(l));
		this.lines.forEach((name, l) -> {
			if(l instanceof SidebarLine) {
				SidebarLine line = (SidebarLine) l;
				if(SidebarDataSender.shouldUpdate(line, phase, world)) {
					this.data.put(line, line.get());
				}
			} else {
				throw new IllegalStateException("Only SidebarLines are permitted");
			}
		});
	}
}
