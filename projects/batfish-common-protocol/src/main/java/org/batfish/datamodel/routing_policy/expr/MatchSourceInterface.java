package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchSourceInterface extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private String _srcInterface;

  @JsonCreator
  private MatchSourceInterface() {}

  public MatchSourceInterface(String srcInterface) {
    _srcInterface = srcInterface;
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
    MatchSourceInterface other = (MatchSourceInterface) obj;
    if (_srcInterface == null) {
      if (other._srcInterface != null) {
        return false;
      }
    } else if (!_srcInterface.equals(other._srcInterface)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public String getList() {
    return _srcInterface;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_srcInterface == null) ? 0 : _srcInterface.hashCode());
    return result;
  }

  public void setList(String srcInterface) {
    _srcInterface = srcInterface;
  }
}
