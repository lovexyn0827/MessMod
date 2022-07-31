package lovexyn0827.mess.rendering.hud.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.entity.Entity;

public class LocalSidebarDataStorage implements SidebarDataSender, HudDataStorage {
	private TreeMap<HudLine, Object> data = new TreeMap<>();
	private List<HudLine> lines = new ArrayList<>();
	
	@Override
	public void updateData(Entity entity) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<HudLine> getCustomLines() {
		return this.lines;
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
	public synchronized void updateData() {
		this.data.clear();
		this.lines.forEach((l) -> {
			if(l instanceof SidebarLine) {
				SidebarLine line = (SidebarLine) l;
				if(line.canGet()) {
					this.data.put(line, line.get());
				}
			} else {
				throw new IllegalStateException("Only SidebarLines are permitted");
			}
		});
	}

}
