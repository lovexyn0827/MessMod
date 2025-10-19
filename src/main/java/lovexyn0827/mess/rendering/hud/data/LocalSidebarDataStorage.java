package lovexyn0827.mess.rendering.hud.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
	private TreeMap<HudLine, Object> data = new TreeMap<>();
	private List<HudLine> lines = new ArrayList<>();
	
	public LocalSidebarDataStorage() {
		this.registerTickingEvents();
	}

	@Override
	public Collection<HudLine> getLines() {
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
		this.data.keySet().removeIf((l) -> !this.lines.contains(l));
		this.lines.forEach((l) -> {
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
