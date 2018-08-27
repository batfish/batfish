package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

@ParametersAreNonnullByDefault
public final class SetNextHop extends Statement {
  private static final String PROP_DESTINATION_VRF = "destinationVrf";
  private static final String PROP_EXPR = "expr";
  /** */
  private static final long serialVersionUID = 1L;

  private boolean _destinationVrf;

  @Nonnull private NextHopExpr _expr;

  @JsonCreator
  private static SetNextHop jsonCreator(
      @Nullable @JsonProperty(PROP_DESTINATION_VRF) Boolean destinationVrf,
      @Nullable @JsonProperty(PROP_EXPR) NextHopExpr expr) {
    checkArgument(destinationVrf != null, "%s must be provided", PROP_DESTINATION_VRF);
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new SetNextHop(expr, destinationVrf);
  }

  public SetNextHop(NextHopExpr expr, boolean destinationVrf) {
    _expr = expr;
    _destinationVrf = destinationVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetNextHop)) {
      return false;
    }
    SetNextHop other = (SetNextHop) obj;
    return _destinationVrf == other._destinationVrf && _expr.equals(other._expr);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    if (_expr.getDiscard()) {
      BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
      bgpRouteBuilder.setDiscard(true);
    }
    Ip nextHop = _expr.getNextHopIp(environment);
    if (nextHop == null) {
      return result;
    }
    environment.getOutputRoute().setNextHopIp(nextHop);
    return result;
  }

  @JsonProperty(PROP_DESTINATION_VRF)
  public boolean getDestinationVrf() {
    return _destinationVrf;
  }

  @JsonProperty(PROP_EXPR)
  @Nonnull
  public NextHopExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_destinationVrf ? 1231 : 1237);
    result = prime * result + _expr.hashCode();
    return result;
  }
}
