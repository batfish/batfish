package org.batfish.symbolic.state;

import javax.annotation.Nonnull;

/**
 * A base class for a {@link StateExpr} for which the only properties are a {@link String} {@code
 * hostname} and a {@link String} {@code vrf} name.
 *
 * <p>This class provides the implementations for {@link #equals(Object)} and {@link #hashCode()},
 * where {@link #hashCode()} is different for different types.
 */
public abstract class VrfStateExpr implements StateExpr {
  @Nonnull private final String _hostname;
  @Nonnull private final String _vrf;

  public VrfStateExpr(@Nonnull String hostname, @Nonnull String vrf) {
    _hostname = hostname;
    _vrf = vrf;
  }

  @Nonnull
  public final String getHostname() {
    return _hostname;
  }

  @Nonnull
  public final String getVrf() {
    return _vrf;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof VrfStateExpr)) {
      return false;
    } else if (!(other.getClass() == getClass())) {
      return false;
    }
    return _hostname.equals(((VrfStateExpr) other)._hostname)
        && _vrf.equals(((VrfStateExpr) other)._vrf);
  }

  @Override
  public final int hashCode() {
    return 31 * 31 * getClass().hashCode() + 31 * _hostname.hashCode() + _vrf.hashCode();
  }

  @Override
  public final String toString() {
    return String.format("%s{%s.%s}", getClass().getSimpleName(), _hostname, _vrf);
  }
}
