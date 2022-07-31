package lovexyn0827.mess.rendering.hud.data;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class RemoteHudDataStorage implements HudDataStorage {
	private Map<HudLine, Object> cache = new TreeMap<>();
	
	public synchronized void pushData(CompoundTag tag) {
		tag.getList("ToRemove", 8).forEach((item) -> this.cache.remove(generateHudLine(item.asString())));
		tag.remove("ToRemove");
		tag.getKeys().forEach((key) -> {
			Tag line = tag.get(key);
			cache.put(generateHudLine(key), line.asString());
		});
	}
	
	private static HudLine generateHudLine(String key) {
		HudLine lineObj = BuiltinHudInfo.BY_TITLE.get(key);
		if(lineObj == null) {
			lineObj = new HudLine.Unknown(key);
		}
		
		return lineObj;
	}

	@Override
	public synchronized int size() {
		return this.cache.size();
	}

	@Override
	public synchronized Object get(HudLine id) {
		return this.cache.get(id);
	}

	@Override
	public synchronized Iterator<Entry<HudLine, Object>> iterator() {
		return this.cache.entrySet().iterator();
	}

}
