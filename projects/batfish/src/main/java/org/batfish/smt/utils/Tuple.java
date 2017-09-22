package org.batfish.smt.utils;

import java.util.Objects;
import javax.annotation.Nullable;

public class Tuple<T1, T2> {

  private T1 _first;

  private T2 _second;

  public Tuple(@Nullable T1 first, @Nullable T2 second) {
    this._first = first;
    this._second = second;
  }

  public T1 getFirst() {
    return _first;
  }

  public T2 getSecond() {
    return _second;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Tuple<?, ?> tuple = (Tuple<?, ?>) o;

    if (_first != null ? !_first.equals(tuple._first) : tuple._first != null) {
      return false;
    }
    return _second != null ? _second.equals(tuple._second) : tuple._second == null;
  }

  @Override public int hashCode() {
    return Objects.hashCode(_first, _second);
  }
}
