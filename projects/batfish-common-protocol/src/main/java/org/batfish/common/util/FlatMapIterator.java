package org.batfish.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/** An implementation of {@code flatMap} for {@link Iterator}. */
public final class FlatMapIterator<Outer, Inner> implements Iterator<Inner> {
  Iterator<Outer> _outer;
  Iterator<Inner> _inner;
  Function<Outer, Iterator<Inner>> _nextInner;

  public FlatMapIterator(Iterator<Outer> outer, Function<Outer, Iterator<Inner>> nextInner) {
    _outer = outer;
    _nextInner = nextInner;
    _inner = null;
  }

  @Override
  public boolean hasNext() {
    if (_inner != null && _inner.hasNext()) {
      return true;
    }
    _inner = null;
    // loop invariant: _inner == null || _inner.hasNext()
    while (_outer.hasNext()) {
      _inner = _nextInner.apply(_outer.next());
      if (_inner.hasNext()) {
        return true;
      }
      _inner = null;
    }
    return false;
  }

  @Override
  public Inner next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return _inner.next();
  }

  public static <Outer, Inner> Iterator<Inner> flatMapIterator(
      Iterator<Outer> outer, Function<Outer, Iterator<Inner>> nextInner) {
    return new FlatMapIterator<>(outer, nextInner);
  }
}
