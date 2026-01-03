package org.batfish.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/** An implementation of {@code flatMap} for {@link Iterator}. */
public final class FlatMapIterator<OuterT, InnerT> implements Iterator<InnerT> {
  Iterator<OuterT> _outer;
  Iterator<InnerT> _inner;
  Function<OuterT, Iterator<InnerT>> _nextInner;

  public FlatMapIterator(Iterator<OuterT> outer, Function<OuterT, Iterator<InnerT>> nextInner) {
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
  public InnerT next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return _inner.next();
  }

  public static <OuterT, InnerT> Iterator<InnerT> flatMapIterator(
      Iterator<OuterT> outer, Function<OuterT, Iterator<InnerT>> nextInner) {
    return new FlatMapIterator<>(outer, nextInner);
  }
}
