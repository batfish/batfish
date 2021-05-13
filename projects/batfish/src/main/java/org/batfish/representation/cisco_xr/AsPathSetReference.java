package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A reference to an as-path-set. */
@ParametersAreNonnullByDefault
public class AsPathSetReference implements AsPathSetExpr {

  public AsPathSetReference(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AsPathSetReference)) {
      return false;
    }
    AsPathSetReference that = (AsPathSetReference) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  private final @Nonnull String _name;
}
