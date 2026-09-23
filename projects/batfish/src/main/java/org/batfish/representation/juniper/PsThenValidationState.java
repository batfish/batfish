package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@code validation-state} action in a Junos routing policy. */
@ParametersAreNonnullByDefault
public final class PsThenValidationState extends PsThen {

  public enum State {
    INVALID,
    UNKNOWN,
    VALID
  }

  public PsThenValidationState(State state) {
    _state = state;
  }

  public @Nonnull State getState() {
    return _state;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // TODO: Model BGP origin-validation state.
    // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/topic-map/bgp_origin_validation.html
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PsThenValidationState && _state == ((PsThenValidationState) o)._state;
  }

  @Override
  public int hashCode() {
    return _state.ordinal();
  }

  private final @Nonnull State _state;
}
