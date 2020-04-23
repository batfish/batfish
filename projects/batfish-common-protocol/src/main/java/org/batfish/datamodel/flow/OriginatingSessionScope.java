package org.batfish.datamodel.flow;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a session that can match flows originating in a given VRF */
@ParametersAreNonnullByDefault
public final class OriginatingSessionScope implements SessionScope {
  @Nonnull private final String _originatingVrf;

  public OriginatingSessionScope(String originatingVrf) {
    _originatingVrf = originatingVrf;
  }

  @Override
  public <T> T accept(SessionScopeVisitor<T> visitor) {
    return visitor.visitOriginatingSessionScope(this);
  }

  @Nonnull
  public String getOriginatingVrf() {
    return _originatingVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OriginatingSessionScope)) {
      return false;
    }
    return _originatingVrf.equals(((OriginatingSessionScope) obj)._originatingVrf);
  }

  @Override
  public int hashCode() {
    return _originatingVrf.hashCode();
  }
}
