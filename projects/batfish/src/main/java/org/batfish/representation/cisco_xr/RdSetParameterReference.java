package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An reference to an {@link RdMatchExpr} named by the value of a named parameter. */
@ParametersAreNonnullByDefault
public final class RdSetParameterReference implements RdMatchExpr {

  public RdSetParameterReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(RdMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetParameterReference(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetParameterReference)) {
      return false;
    }
    RdSetParameterReference that = (RdSetParameterReference) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Nonnull private final String _name;
}
