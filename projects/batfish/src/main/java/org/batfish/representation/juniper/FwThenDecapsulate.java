package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Decapsulate a packet before an optional lookup in another routing instance. */
public final class FwThenDecapsulate implements FwThen {

  public enum Type {
    GRE,
    GRE_IN_UDP,
    MPLS_IN_UDP
  }

  private final @Nullable String _routingInstance;
  private final @Nonnull Type _type;

  public FwThenDecapsulate(@Nonnull Type type, @Nullable String routingInstance) {
    _type = type;
    _routingInstance = routingInstance;
  }

  public @Nullable String getRoutingInstance() {
    return _routingInstance;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenDecapsulate(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FwThenDecapsulate)) {
      return false;
    }
    FwThenDecapsulate that = (FwThenDecapsulate) o;
    return _type == that._type && Objects.equals(_routingInstance, that._routingInstance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _routingInstance);
  }
}
