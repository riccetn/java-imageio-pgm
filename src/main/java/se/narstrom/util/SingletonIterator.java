package se.narstrom.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingletonIterator<T> implements Iterator<T> {
	private T object;
	private boolean called = false;

	private SingletonIterator(T object) {
		this.object = object;
	}

	public static <T> SingletonIterator<T> of(T object) {
		return new SingletonIterator<T>(object);
	}

	@Override
	public boolean hasNext() {
		return !this.called;
	}

	@Override
	public T next() {
		if(this.called)
			throw new NoSuchElementException();
		this.called = true;
		return this.object;
	}
}
