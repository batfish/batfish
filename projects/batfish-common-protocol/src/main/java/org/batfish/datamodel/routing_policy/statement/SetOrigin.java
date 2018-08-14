package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;

public class SetOrigin extends Statement {

  private static final String PROP_ORIGIN_TYPE = "originType";

  /** */
  private static final long serialVersionUID = 1L;

  private OriginExpr _origin;

  @JsonCreator
  private SetOrigin() {}

  public SetOrigin(OriginExpr origin) {
    _origin = origin;
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
    SetOrigin other = (SetOrigin) obj;
    if (_origin == null) {
      if (other._origin != null) {
        return false;
      }
    } else if (!_origin.equals(other._origin)) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    BgpRoute.Builder bgpRoute = (BgpRoute.Builder) environment.getOutputRoute();
    OriginType originType = _origin.evaluate(environment);
    bgpRoute.setOriginType(originType);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setOriginType(originType);
    }
    Result result = new Result();
    return result;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginExpr getOriginType() {
    return _origin;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_origin == null) ? 0 : _origin.hashCode());
    return result;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public void setOriginType(OriginExpr origin) {
    _origin = origin;
  }
}
