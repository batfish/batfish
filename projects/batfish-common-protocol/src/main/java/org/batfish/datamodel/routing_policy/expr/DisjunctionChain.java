package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** Juniper subroutine chain */
public class DisjunctionChain extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private List<BooleanExpr> _subroutines;

  @JsonCreator
  private DisjunctionChain() {}

  public DisjunctionChain(List<BooleanExpr> subroutines) {
    _subroutines = subroutines;
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
    DisjunctionChain other = (DisjunctionChain) obj;
    if (_subroutines == null) {
      if (other._subroutines != null) {
        return false;
      }
    } else if (!_subroutines.equals(other._subroutines)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    Result subroutineResult = new Result();
    subroutineResult.setFallThrough(true);
    for (BooleanExpr subroutine : _subroutines) {
      subroutineResult = subroutine.evaluate(environment);
      if (subroutineResult.getExit()) {
        return subroutineResult;
      } else if (!subroutineResult.getFallThrough() && subroutineResult.getBooleanValue()) {
        subroutineResult.setReturn(true);
        return subroutineResult;
      }
    }
    if (!subroutineResult.getFallThrough()) {
      return subroutineResult;
    } else {
      String defaultPolicy = environment.getDefaultPolicy();
      if (defaultPolicy != null) {
        CallExpr callDefaultPolicy = new CallExpr(environment.getDefaultPolicy());
        Result defaultPolicyResult = callDefaultPolicy.evaluate(environment);
        return defaultPolicyResult;
      } else {
        throw new BatfishException("Default policy not set");
      }
    }
  }

  public List<BooleanExpr> getSubroutines() {
    return _subroutines;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_subroutines == null) ? 0 : _subroutines.hashCode());
    return result;
  }

  public void setSubroutines(List<BooleanExpr> subroutines) {
    _subroutines = subroutines;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _subroutines + ">";
  }
}
