package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public class SetNextHop extends Statement {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _destinationVrf;

  private NextHopExpr _expr;

  @JsonCreator
  private SetNextHop() {}

  public SetNextHop(NextHopExpr expr, boolean destinationVrf) {
    _expr = expr;
    _destinationVrf = destinationVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SetNextHop other = (SetNextHop) obj;
    if (_destinationVrf != other._destinationVrf) {
      return false;
    }
    if (_expr == null) {
      if (other._expr != null) {
        return false;
      }
    } else if (!_expr.equals(other._expr)) {
      return false;
    }
    return true;
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

  public boolean getDestinationVrf() {
    return _destinationVrf;
  }

  public NextHopExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_destinationVrf ? 1231 : 1237);
    result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
    return result;
  }

  public void setDestinationVrf(boolean destinationVrf) {
    _destinationVrf = destinationVrf;
  }

  public void setExpr(NextHopExpr expr) {
    _expr = expr;
  }
}
