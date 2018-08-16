package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

public class SetLocalPreference extends Statement {

  private static final String PROP_LOCAL_PREFERENCE = "localPreference";

  /** */
  private static final long serialVersionUID = 1L;

  private IntExpr _localPreference;

  @JsonCreator
  private SetLocalPreference() {}

  public SetLocalPreference(IntExpr localPreference) {
    _localPreference = localPreference;
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
    SetLocalPreference other = (SetLocalPreference) obj;
    if (_localPreference == null) {
      if (other._localPreference != null) {
        return false;
      }
    } else if (!_localPreference.equals(other._localPreference)) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    BgpRoute.Builder bgpBuilder = (BgpRoute.Builder) environment.getOutputRoute();
    int localPreference = _localPreference.evaluate(environment);
    bgpBuilder.setLocalPreference(localPreference);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setLocalPreference(localPreference);
    }
    return result;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public IntExpr getLocalPreference() {
    return _localPreference;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_localPreference == null) ? 0 : _localPreference.hashCode());
    return result;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public void setLocalPreference(IntExpr localPreference) {
    _localPreference = localPreference;
  }
}
