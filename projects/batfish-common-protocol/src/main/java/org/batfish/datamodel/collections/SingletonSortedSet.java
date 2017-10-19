package org.batfish.datamodel.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;

public class SingletonSortedSet<T extends Comparable<T>> extends AbstractSet<T>
    implements SortedSet<T> {

  private class Comparator implements java.util.Comparator<T> {

    private Comparator() {}

    @Override
    public int compare(T o1, T o2) {
      return o1.compareTo(o2);
    }
  }

  private static class Iterator<T> implements java.util.Iterator<T> {

    private final T _elem;

    private boolean _hasNext;

    private Iterator(T elem) {
      _elem = elem;
      _hasNext = true;
    }

    @Override
    public boolean hasNext() {
      return _hasNext;
    }

    @Override
    public T next() {
      if (_hasNext) {
        _hasNext = false;
        return _elem;
      } else {
        return null;
      }
    }
  }

  private static final String MODIFICATION_ERROR_MSG =
      "Cannot modify immutable " + SingletonSortedSet.class.getName();

  public static <T extends Comparable<T>> SingletonSortedSet<T> of(T elem) {
    if (elem == null) {
      throw new IllegalArgumentException("Argument must be non-null");
    }
    return new SingletonSortedSet<T>(elem);
  }

  private SingletonSortedSet<T>.Comparator _comparator;

  private final T _elem;

  private SingletonSortedSet(T elem) {
    _comparator = new Comparator();
    _elem = elem;
  }

  @Override
  public boolean add(T e) {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public java.util.Comparator<? super T> comparator() {
    return _comparator;
  }

  @Override
  public boolean contains(Object o) {
    return _elem.equals(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!_elem.equals(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public T first() {
    return _elem;
  }

  @Override
  public SortedSet<T> headSet(T toElement) {
    if (_elem.compareTo(toElement) < 0) {
      return this;
    } else {
      return Collections.emptySortedSet();
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public java.util.Iterator<T> iterator() {
    return new Iterator<T>(_elem);
  }

  @Override
  public T last() {
    return _elem;
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException(MODIFICATION_ERROR_MSG);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public SortedSet<T> subSet(T fromElement, T toElement) {
    if (_elem.compareTo(fromElement) >= 0 && _elem.compareTo(toElement) < 0) {
      return this;
    } else {
      return Collections.emptySortedSet();
    }
  }

  @Override
  public SortedSet<T> tailSet(T fromElement) {
    if (_elem.compareTo(fromElement) >= 0) {
      return this;
    } else {
      return Collections.emptySortedSet();
    }
  }

  @Override
  public Object[] toArray() {
    return new Object[] {_elem};
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E> E[] toArray(E[] a) {
    Class<?> aElemType = a.getClass().getComponentType();
    Class<?> elemType = _elem.getClass();
    if (!aElemType.isAssignableFrom(elemType)) {
      throw new ClassCastException(
          elemType.getCanonicalName() + " is not a subclass of " + aElemType.getCanonicalName());
    }
    int aLen = a.length;
    if (aLen > 0) {
      a[0] = (E) _elem;
      if (aLen > 1) {
        a[1] = null;
      }
      return a;
    } else {
      Object[] newArray = new Object[] {_elem};
      return (E[]) newArray;
    }
  }
}
