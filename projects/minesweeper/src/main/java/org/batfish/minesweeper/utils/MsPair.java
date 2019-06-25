package org.batfish.minesweeper.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A generic pair of comparable objects */
@ParametersAreNonnullByDefault
public class MsPair<T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
    implements Serializable, Comparable<MsPair<T1, T2>> {

  @Nullable private final T1 _first;
  @Nullable private final T2 _second;

  public MsPair(@Nullable T1 t1, @Nullable T2 t2) {
    _first = t1;
    _second = t2;
  }

  @Override
  public int compareTo(MsPair<T1, T2> rhs) {
    return Comparator.comparing(MsPair<T1, T2>::getFirst, Comparator.nullsFirst(T1::compareTo))
        .thenComparing(MsPair::getSecond, Comparator.nullsFirst(T2::compareTo))
        .compare(this, rhs);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MsPair)) {
      return false;
    }

    MsPair<?, ?> other = (MsPair<?, ?>) obj;
    return Objects.equals(_first, other._first) && Objects.equals(_second, other._second);
  }

  @Nullable
  @JsonIgnore
  public final T1 getFirst() {
    return _first;
  }

  @JsonIgnore
  @Nullable
  public final T2 getSecond() {
    return _second;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_first, _second);
  }

  @Override
  public String toString() {
    return "<" + _first + ":" + _second + ">";
  }
}
