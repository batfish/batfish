package org.batfish.symbolic.state;

import javax.annotation.Nonnull;

/**
 * A base class for a {@link StateExpr} for which the only properties are a {@link String} {@code
 * hostname} and a {@link String} {@code interface} name.
 *
 * <p>This class provides the implementations for {@link #equals(Object)} and {@link #hashCode()},
 * where {@link #hashCode()} is different for different types.
 */
public abstract class InterfaceStateExpr implements StateExpr {
  @Nonnull private final String _hostname;
  @Nonnull private final String _interface;

  public InterfaceStateExpr(@Nonnull String hostname, @Nonnull String iface) {
    _hostname = hostname;
    _interface = iface;
  }

  @Nonnull
  public final String getHostname() {
    return _hostname;
  }

  @Nonnull
  public final String getInterface() {
    return _interface;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof InterfaceStateExpr)) {
      return false;
    } else if (!(other.getClass() == getClass())) {
      return false;
    }
    return _hostname.equals(((InterfaceStateExpr) other)._hostname)
        && _interface.equals(((InterfaceStateExpr) other)._interface);
  }

  @Override
  public final int hashCode() {
    return 31 * 31 * getClass().hashCode() + 31 * _hostname.hashCode() + _interface.hashCode();
  }

  @Override
  public final String toString() {
    return String.format("%s{%s[%s]}", getClass().getSimpleName(), _hostname, _interface);
  }
}
