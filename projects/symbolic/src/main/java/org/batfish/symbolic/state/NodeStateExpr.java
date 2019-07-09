package org.batfish.symbolic.state;

import javax.annotation.Nonnull;

/**
 * A base class for a {@link StateExpr} for which the only property is a {@link String} {@code
 * hostname}.
 *
 * <p>This class provides the implementations for {@link #equals(Object)} and {@link #hashCode()},
 * where {@link #hashCode()} is different for different types.
 */
public abstract class NodeStateExpr implements StateExpr {
  @Nonnull private final String _hostname;

  public NodeStateExpr(@Nonnull String hostname) {
    _hostname = hostname;
  }

  @Nonnull
  public final String getHostname() {
    return _hostname;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof NodeStateExpr)) {
      return false;
    } else if (!(other.getClass() == getClass())) {
      return false;
    }
    return _hostname.equals(((NodeStateExpr) other)._hostname);
  }

  @Override
  public final int hashCode() {
    return 31 * getClass().hashCode() + _hostname.hashCode();
  }

  @Override
  public final String toString() {
    return String.format("%s{%s}", getClass().getSimpleName(), _hostname);
  }
}
