package org.batfish.datamodel.flow;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby return traffic is forwarded according to the result of a lookup
 * on the FIB of the interface on which the return traffic is received.
 */
@ParametersAreNonnullByDefault
public final class FibLookup implements SessionAction {

  public static final FibLookup INSTANCE = new FibLookup();

  private FibLookup() {}

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitFibLookup(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof FibLookup;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
