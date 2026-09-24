package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** A {@code from state} match condition in a Junos routing policy. */
@ParametersAreNonnullByDefault
public final class PsFromState extends PsFrom {

  public enum State {
    ACTIVE,
    INACTIVE
  }

  public PsFromState(State state) {
    _state = state;
  }

  public @Nonnull State getState() {
    return _state;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    // TODO: Implement active and inactive route matching.
    // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/topic-map/basic-routing-policies.html
    return BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PsFromState && _state == ((PsFromState) o)._state;
  }

  @Override
  public int hashCode() {
    return _state.ordinal();
  }

  private final @Nonnull State _state;
}
