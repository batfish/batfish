package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;

public class VarRouteType extends RouteTypeExpr {

  private String _var;

  @JsonCreator
  private VarRouteType() {}

  public VarRouteType(String var) {
    _var = var;
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
    VarRouteType other = (VarRouteType) obj;
    if (_var == null) {
      if (other._var != null) {
        return false;
      }
    } else if (!_var.equals(other._var)) {
      return false;
    }
    return true;
  }

  @Override
  public RouteType evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public String getVar() {
    return _var;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_var == null) ? 0 : _var.hashCode());
    return result;
  }

  public void setVar(String var) {
    _var = var;
  }
}
