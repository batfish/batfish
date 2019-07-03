package org.batfish.datamodel;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** A {@link FibAction} indicating that lookup should be delegated to another VRF. */
@ParametersAreNonnullByDefault
public class FibNextVrf implements FibAction {

  private final @Nonnull String _nextVrf;

  public FibNextVrf(String nextVrf) {
    _nextVrf = nextVrf;
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibNextVrf(this);
  }

  public @Nonnull String getNextVrf() {
    return _nextVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FibNextVrf)) {
      return false;
    }
    return _nextVrf.equals(((FibNextVrf) obj)._nextVrf);
  }

  @Override
  public int hashCode() {
    return _nextVrf.hashCode();
  }
}
