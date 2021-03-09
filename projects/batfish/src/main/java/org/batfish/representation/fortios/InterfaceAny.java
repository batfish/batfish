package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** FortiOS datamodel component representing the special-case 'any' interface. */
public final class InterfaceAny implements InterfaceOrZone, Serializable {
  public static final InterfaceAny INSTANCE = new InterfaceAny();
  public static final String name = "any";

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof InterfaceAny;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  private InterfaceAny() {}
}
