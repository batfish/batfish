package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import javax.annotation.Nullable;

public abstract class InterfaceAddress implements Serializable, Comparable<InterfaceAddress> {

  @Override
  public final int compareTo(InterfaceAddress o) {
    if (this == o) {
      return 0;
    }
    int ret = getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    if (ret != 0) {
      return ret;
    }
    return compareSameClass(o);
  }

  protected abstract int compareSameClass(InterfaceAddress o);

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  @JsonCreator
  private static InterfaceAddress jsonCreator(@Nullable String text) {
    checkArgument(text != null);
    if (text.startsWith(UnnumberedAddress.STR_PREFIX + ":")) {
      return UnnumberedAddress.parse(text);
    }
    try {
      // Try the common case first
      return ConcreteInterfaceAddress.parse(text);
    } catch (IllegalArgumentException e) {
      return LinkLocalAddress.parse(text);
    }
  }
}
