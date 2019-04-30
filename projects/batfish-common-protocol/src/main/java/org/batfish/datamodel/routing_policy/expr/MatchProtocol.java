package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.RoutingProtocol.ISIS_EL1;
import static org.batfish.datamodel.RoutingProtocol.ISIS_EL2;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L1;
import static org.batfish.datamodel.RoutingProtocol.ISIS_L2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has a route that matches a given
 * {@link RoutingProtocol}.
 */
@ParametersAreNonnullByDefault
public final class MatchProtocol extends BooleanExpr {
  private static final String PROP_PROTOCOL = "protocol";
  private static final long serialVersionUID = 1L;
  private static final EnumSet<RoutingProtocol> ISIS_EXPANSION =
      EnumSet.of(ISIS_L1, ISIS_L2, ISIS_EL1, ISIS_EL2);

  /** TODO: ideally, this should be a list of protocols, treated as a disjunction (match any) */
  @Nonnull private final RoutingProtocol _protocol;

  public MatchProtocol(RoutingProtocol protocol) {
    _protocol = protocol;
  }

  @JsonCreator
  private static MatchProtocol create(
      @Nullable @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol) {
    checkArgument(protocol != null, "MatchProtocol missing %s", PROP_PROTOCOL);
    return new MatchProtocol(protocol);
  }

  @Override
  public Result evaluate(Environment environment) {
    // Workaround: Treat ISIS_ANY as a special value
    if (_protocol == RoutingProtocol.ISIS_ANY) {
      return new Result(ISIS_EXPANSION.contains(environment.getOriginalRoute().getProtocol()));
    }
    return new Result(environment.getOriginalRoute().getProtocol().equals(_protocol));
  }

  @Nonnull
  @JsonProperty(PROP_PROTOCOL)
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchProtocol)) {
      return false;
    }
    MatchProtocol that = (MatchProtocol) o;
    return _protocol == that._protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocol.ordinal());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _protocol.protocolName() + ">";
  }
}
