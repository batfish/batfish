package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class SetVarMetricType extends Statement {

  private String _var;

  @JsonCreator
  private SetVarMetricType() {}

  public SetVarMetricType(String var) {
    _var = var;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetVarMetricType(this, arg);
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
    SetVarMetricType other = (SetVarMetricType) obj;
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
  public Result execute(Environment environment) {
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
