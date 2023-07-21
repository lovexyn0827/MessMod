package lovexyn0827.mess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

public class LockableList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 202307200058L;
	private boolean locked = false;
	
	private LockableList() {}
	
	/**
	 * Once a LockableList is locked, no further modification will be permitted, or an exception will be thrown.
	 */
	public void lock() {
		this.locked = true;
	}
	
	public static <T> LockableList<T> create() {
		return new LockableList<T>();
	}
	
	@Override
	public boolean add(T obj) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.add(obj);
		}
	}
	
	@Override
	public void add(int index, T obj) {
		if(this.locked) {
			throw new LockedException();
		} else {
			super.add(obj);
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends T> obj) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.addAll(obj);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr<>(super.iterator());
	}

	@Override
	public boolean remove(Object o) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.remove(o);
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.addAll(index, c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.removeAll(c);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.retainAll(c);
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
	public T set(int index, T element) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.set(index, element);
		}
	}

	@Override
	public T remove(int index) {
		if(this.locked) {
			throw new LockedException();
		} else {
			return super.remove(index);
		}
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ListItr<>(super.listIterator());
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ListItr<>(super.listIterator(index));
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
			if(LockableList.this.locked) {
				throw new LockedException();
			} else {
				this.backend.remove();
			}
		}
	}
	
	private class ListItr<E> implements ListIterator<E> {
		private ListIterator<E> backend;

		protected ListItr(ListIterator<E> backend) {
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
			if(LockableList.this.locked) {
				throw new LockedException();
			} else {
				this.backend.remove();
			}
		}

		@Override
		public boolean hasPrevious() {
			return this.backend.hasPrevious();
		}

		@Override
		public E previous() {
			return this.backend.previous();
		}

		@Override
		public int nextIndex() {
			return this.backend.nextIndex();
		}

		@Override
		public int previousIndex() {
			return this.backend.previousIndex();
		}

		@Override
		public void set(E e) {
			if(LockableList.this.locked) {
				throw new LockedException();
			} else {
				this.backend.set(e);
			}
		}

		@Override
		public void add(E e) {
			if(LockableList.this.locked) {
				throw new LockedException();
			} else {
				this.backend.add(e);
			}
		}
	}
}
