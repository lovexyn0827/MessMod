package lovexyn0827.mess.util;

import java.util.Collection;
import java.util.Iterator;
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
		return new DelegatingSet<>(super.keySet());
	}

	@Override
	public Collection<V> values() {
		return new DelegatingCollection<>(super.values());
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new DelegatingSet<>(super.entrySet());
	}
	
	private class DelegatingCollection<T> implements Collection<T> {
		private final Collection<T> backend;
		
		public DelegatingCollection(Collection<T> backend) {
			this.backend = backend;
		}

		@Override
		public int size() {
			return this.backend.size();
		}

		@Override
		public boolean isEmpty() {
			return this.backend.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return this.backend.contains(o);
		}

		@Override
		public Iterator<T> iterator() {
			return new Itr<>(this.backend.iterator());
		}

		@Override
		public Object[] toArray() {
			return this.backend.toArray();
		}

		@Override
		public <A> A[] toArray(A[] a) {
			return this.backend.toArray(a);
		}

		@Override
		public boolean add(T e) {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				return this.backend.add(e);
			}
		}

		@Override
		public boolean remove(Object o) {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				return this.backend.remove(o);
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return this.backend.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				return this.backend.addAll(c);
			}
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				return this.backend.retainAll(c);
			}
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				return this.backend.removeAll(c);
			}
		}

		@Override
		public void clear() {
			if(LockableLinkedHashMap.this.locked) {
				throw new LockedException();
			} else {
				this.backend.clear();
			}
		}
		
		private class Itr<E> implements Iterator<E> {
			private Iterator<E> backend;

			protected Itr(Iterator<E> backend) {
				this.backend = backend;
			}
			
			@Override
			public final boolean hasNext() {
				return this.backend.hasNext();
			}

			@Override
			public final E next() {
				return this.backend.next();
			}
			
			@Override
			public void remove() {
				if(LockableLinkedHashMap.this.locked) {
					throw new LockedException();
				} else {
					this.backend.remove();
				}
			}
		}
	}
	
	private final class DelegatingSet<T> extends DelegatingCollection<T> implements Set<T> {
		public DelegatingSet(Set<T> backend) {
			super(backend);
		}
	}
}
