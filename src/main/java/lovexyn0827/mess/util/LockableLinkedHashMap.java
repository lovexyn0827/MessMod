package lovexyn0827.mess.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LockableLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 202307210250L;
	private boolean locked;
	
	public LockableLinkedHashMap() {
		super();
	}
	
	public void lock() {
		this.locked = true;
	}
	
	@Override
	public V put(K key, V value) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.put(key, value);
		}
	}

	@Override
	public V remove(Object key) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.remove(key);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(this.locked) {
			throw new LockedException();
		} else {
			super.putAll(m);
		}
	}

	@Override
	public void clear() {
		if(this.locked) {
			throw new LockedException();
		} else {
			super.clear();
		}
	}

	@Override
	public Set<K> keySet() {
		// TODO 
		return super.keySet();
	}

	@Override
	public Collection<V> values() {
		// TODO 
		return super.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		// TODO 
		return super.entrySet();
	}
}
