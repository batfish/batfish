package org.batfish.symbolic.state;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A base class for a {@link StateExpr} for which the only properties are those of an edge: a source
 * and destination pair of {@link String} {@code hostname} and a {@link String} {@code interface}
 * name.
 *
 * <p>This class provides the implementations for {@link #equals(Object)} and {@link #hashCode()},
 * where {@link #hashCode()} is different for different types.
 */
public abstract class EdgeStateExpr implements StateExpr {
  private final @Nonnull String _srcHostname;
  private final @Nonnull String _srcInterface;
  private final @Nonnull String _dstHostname;
  private final @Nonnull String _dstInterface;

  public EdgeStateExpr(
      @Nonnull String srcNode,
      @Nonnull String srcIface,
      @Nonnull String dstNode,
      @Nonnull String dstIface) {
    _srcHostname = srcNode;
    _srcInterface = srcIface;
    _dstHostname = dstNode;
    _dstInterface = dstIface;
  }

  public final @Nonnull String getDstIface() {
    return _dstInterface;
  }

  public final @Nonnull String getDstNode() {
    return _dstHostname;
  }

  public final @Nonnull String getSrcIface() {
    return _srcInterface;
  }

  public final @Nonnull String getSrcNode() {
    return _srcHostname;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof EdgeStateExpr)) {
      return false;
    } else if (!(other.getClass() == getClass())) {
      return false;
    }
    return _srcHostname.equals(((EdgeStateExpr) other)._srcHostname)
        && _srcInterface.equals(((EdgeStateExpr) other)._srcInterface)
        && _dstHostname.equals(((EdgeStateExpr) other)._dstHostname)
        && _dstInterface.equals(((EdgeStateExpr) other)._dstInterface);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), _srcHostname, _srcInterface, _dstHostname, _dstInterface);
  }

  @Override
  public final String toString() {
    return String.format(
        "%s{%s[%s]->%s[%s]}",
        getClass().getSimpleName(), _srcHostname, _srcInterface, _dstHostname, _dstInterface);
  }
}
