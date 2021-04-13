package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A reference to a named {@link RdSet}. */
@ParametersAreNonnullByDefault
public final class RdSetReference implements RdMatchExpr {

  public RdSetReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(RdMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetReference(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetReference)) {
      return false;
    }
    RdSetReference that = (RdSetReference) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Nonnull private final String _name;
}
