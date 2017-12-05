package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Juniper subroutine chain */
public class ConjunctionChain extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private List<BooleanExpr> _subroutines;

  @JsonCreator
  private ConjunctionChain() {}

  public ConjunctionChain(List<BooleanExpr> subroutines) {
    _subroutines = subroutines;
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
    for (BooleanExpr conjunct : _subroutines) {
      childSources.addAll(conjunct.collectSources(parentSources, routingPolicies, w));
    }
    return childSources.build();
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
    ConjunctionChain other = (ConjunctionChain) obj;
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
      } else if (!subroutineResult.getFallThrough() && !subroutineResult.getBooleanValue()) {
        subroutineResult.setReturn(false);
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
