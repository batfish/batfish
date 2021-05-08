package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A route-policy variable whose value is the name of an as-path-set. */
@ParametersAreNonnullByDefault
public final class AsPathSetVariable implements AsPathSetExpr {

  public AsPathSetVariable(String var) {
    _var = var;
  }

  public @Nonnull String getVar() {
    return _var;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AsPathSetVariable)) {
      return false;
    }
    AsPathSetVariable that = (AsPathSetVariable) o;
    return _var.equals(that._var);
  }

  @Override
  public int hashCode() {
    return _var.hashCode();
  }

  private final @Nonnull String _var;
}
