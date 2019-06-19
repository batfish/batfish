package org.batfish.datamodel.flow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby return traffic is be accepted by the VRF from which it
 * originated.
 */
@ParametersAreNonnullByDefault
public final class AcceptVrf implements SessionAction {

  private final @Nonnull String _vrf;

  public AcceptVrf(String vrf) {
    _vrf = vrf;
  }

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitAcceptVrf(this);
  }

  /** The name of the originating VRF for this session. */
  public @Nonnull String getVrf() {
    return _vrf;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AcceptVrf)) {
      return false;
    }
    return _vrf.equals(((AcceptVrf) o)._vrf);
  }

  @Override
  public int hashCode() {
    return _vrf.hashCode();
  }
}
