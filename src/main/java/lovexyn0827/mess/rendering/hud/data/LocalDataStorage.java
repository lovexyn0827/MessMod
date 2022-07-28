package lovexyn0827.mess.rendering.hud.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import lovexyn0827.mess.mixins.BoatEntityAccessor;
import lovexyn0827.mess.rendering.hud.EntityHudUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class LocalDataStorage implements HudDataSenderer, HudDataStorage {
	private Map<HudLine, Object> data = new TreeMap<>();
	private List<HudLine> customLines = new ArrayList<>();

	@Override
	public synchronized void updateData(Entity entity) {
		this.data.clear();
		if (entity == null) return;
		for(HudLine l : BuiltinHudInfo.values()) {
			if(l.canGetFrom(entity)) {
				this.data.put(l, l.getFrom(entity));
			}
		}
		
		this.customLines.forEach((f) -> {
			if(f.canGetFrom(entity)) {
				this.data.put(f, f.getFrom(entity));
			}
		});
	}

	@Override
	public List<HudLine> getCustomLines() {
		return this.customLines;
	}

	@Override
	public boolean addLine(HudLine line) {
		if(this.customLines.contains(line)) {
			return false;
		} else {
			this.customLines.add(line);
			return true;
		}
	}

	@Override
	public boolean removeField(String name) {
		return this.customLines.removeIf(((line) -> line.getName().equals(name)));
	}

	@Override
	@Deprecated
	public void pushData(String name, Object data) {
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
