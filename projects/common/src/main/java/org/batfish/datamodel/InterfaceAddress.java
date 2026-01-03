package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import javax.annotation.Nullable;

public abstract class InterfaceAddress implements Serializable, Comparable<InterfaceAddress> {

  @JsonCreator
  private static InterfaceAddress jsonCreator(@Nullable String text) {
    checkArgument(text != null);
    try {
      // Try the common case first
      return ConcreteInterfaceAddress.parse(text);
    } catch (IllegalArgumentException e) {
      return LinkLocalAddress.parse(text);
    }
  }
}
